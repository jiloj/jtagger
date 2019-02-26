package ml.spark

import scala.collection.Map
import ml.{Tagger, TaggerDefinition}
import model.base.Clue
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession

/**
  * The bare tagger functionality.
  */
class NaiveBayesTagger private (val model: PipelineModel) extends Tagger[Clue] {
  /**
    * Tag an input with a given output label.
    *
    * @param input The input clue that is to be tagged.
    * @return The semantic category label that is the result of the transformation.
    */
  def tag(input: Clue): Int = {
    val spark = SparkSession
      .builder
      .appName("jtagger")
      .config("spark.master", "local")
      .getOrCreate()
    import spark.implicits._

    val df = Seq(input).map { clue =>
      Clue.unapply(clue).get
    }.toDF("question", "answer", "category", "value", "round")
    val classification = model.transform(df)
    println("Here")
    for {
      column <- classification.columns
    } yield {
      println(column)
      println(classification.first().getAs(column))
      println()
    }

    val semcat = classification.first().getAs[Double]("prediction").toInt

    semcat
  }
}

/**
  * Definition of the NaiveBayesTagger creation and persistence and lifetime behavior.
  */
object NaiveBayesTagger extends TaggerDefinition[NaiveBayesTagger, Clue] {
  val spark = SparkSession
    .builder
    .appName("jtagger")
    .config("spark.master", "local")
    .getOrCreate()
  spark.udf.register("prefixTokens", prefixTokens _)
  import spark.implicits._

  private val questionPipeline = createTextFeaturePipeline("question", "question_features")
  private val answerPipeline = createTextFeaturePipeline("answer", "answer_features")
  private val categoryPipeline = createTextFeaturePipeline("category", "category_features")
  private val assembler = new VectorAssembler()
    .setInputCols(Array("question_features", "answer_features", "category_features"))
    .setOutputCol("features")
  private val featurePipeline = new Pipeline()
    .setStages(Array(questionPipeline, answerPipeline, categoryPipeline, assembler))
  private val naiveBayes = new NaiveBayes()
    .setModelType("multinomial")
    .setSmoothing(1)
    .setFeaturesCol("features")
    .setLabelCol("label")
  private val classifierPipeline = new Pipeline()
    .setStages(Array(featurePipeline, naiveBayes))

  /**
    * Creates a new tagger from the provided training dataset.
    *
    * @param train The map from input to output to serve as training data.
    * @return The created Tagger from the provided training data.
    */
  override def create(train: Map[Clue, Int]): NaiveBayesTagger = {
    val df = train.toSeq.map { tup =>
      // TODO: How to use shapeless here.
      val c = tup._1
      (c.question, c.answer, c.category, c.value, c.round, tup._2)
    }.toDF("question", "answer", "category", "value", "round", "label")

    val model = classifierPipeline.fit(df)
    new NaiveBayesTagger(model)
  }

  /**
    * Saves the tagger to disk at the given file.
    *
    * @param tagger The tagger to save to disk.
    * @param filename The file to marshall the tagger into.
    */
  override def persist(tagger: NaiveBayesTagger, filename: String): Unit = {
    tagger.model.write.overwrite().save(filename)
  }

  /**
    * Loads a NaiveBayesTagger from a provided filename.
    *
    * @param filename The filename to load from.
    * @return The loaded Tagger.
    */
  override def load(filename: String): NaiveBayesTagger = {
    val model = PipelineModel.load(filename)
    new NaiveBayesTagger(model)
  }

  /**
    * Creates the feature pipeline for a certain column on a data frame. This column is assumed to have free text data.
    *
    * @param inputCol The name of the input column on the pipeline.
    * @param outputCol The name of the output column on the pipeline.
    */
  private def createTextFeaturePipeline(inputCol: String, outputCol: String): Pipeline = {
    val tokenizer = new Tokenizer()
      .setInputCol(inputCol)
      .setOutputCol(inputCol + "_tokens")

    // TODO: Fix this string abomination
    val tmp = "["+ inputCol + "]"
    val prefixTransformer = new SQLTransformer()
      .setStatement(s"SELECT *, prefixTokens(${tokenizer.getOutputCol}, $tmp)" +
                    s"as prefix_${tokenizer.getOutputCol} FROM __THIS__")

    val counter = new CountVectorizer()
      .setInputCol(tokenizer.getOutputCol)
      .setOutputCol(inputCol + "_counts")

    val idf = new IDF().setInputCol(counter.getOutputCol).setOutputCol(outputCol)

    new Pipeline()
      .setStages(Array(tokenizer, prefixTransformer, counter, idf))
  }

  /**
    * A helper function to prefix all items in a given vector.
    *
    * @param tokens The tokens to prefix.
    * @param prefix The prefix to prepend.
    * @return The mapped vector.
    */
  private def prefixTokens(tokens: Seq[String], prefix: String): Seq[String] = {
    tokens.map(prefix + _)
  }
}
