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
class NaiveBayesTagger private (val model: PipelineModel) extends Tagger[Clue, String] {
  private val spark = SparkSession.builder.getOrCreate()
  import spark.implicits._

  /**
    * Tag an input with a given output label.
    *
    * @param input The input clue that is to be tagged.
    * @return The semantic category label that is the result of the transformation.
    */
  def tag(input: Clue): String = {
    val classification = model.transform(Seq(input).toDF())
    val semcat = classification.first().getAs[String]("semcat")

    semcat
  }
}

/**
  * Implementation of a tagger's creation and persistence.
  */
object NaiveBayesTagger extends TaggerDefinition[NaiveBayesTagger, Clue, String] {
  private val spark = SparkSession.builder.getOrCreate()
  spark.udf.register("prefixTokens", prefixTokens _)
  import spark.implicits._

  private val questionPipeline = createTextFeaturePipeline("question", "question_features")
  private val answerPipeline = createTextFeaturePipeline("answer", "answer_features")
  private val categoryPipeline = createTextFeaturePipeline("category", "category_features")
  private val assembler = new VectorAssembler()
    .setInputCols(Array("question_features", "answer_features", "category_features"))
    .setOutputCol("features")
  private val featurePipeline = new Pipeline()
    .setStages(Array(questionPipeline, answerPipeline, categoryPipeline))
  private val naiveBayes = new NaiveBayes()
    .setModelType("multinomial")
    .setSmoothing(1)
    .setFeaturesCol("features")
    .setLabelCol("semcat")
  private val classifierPipeline = new Pipeline()
    .setStages(Array(featurePipeline, naiveBayes))

  /**
    * Creates a new tagger from the provided training dataset.
    *
    * @param train The map from input to output to serve as training data.
    * @return The created Tagger from the provided training data.
    */
  def create(train: Map[Clue, String]): NaiveBayesTagger = {
    val df = train.toSeq.map { tup =>
      val c = tup._1
      (c.question, c.answer, c.value, c.category, c.round, tup._2)
    }.toDF("question", "answer", "value", "category", "round", "semcat")

    val model = classifierPipeline.fit(df)
    new NaiveBayesTagger(model)
  }

  /**
    * Saves the tagger to disk at the given file.
    *
    * @param tagger The tagger to save to disk.
    * @param filename The file to marshall the tagger into.
    */
  def persist(tagger: NaiveBayesTagger, filename: String): Unit = {
    tagger.model.write.overwrite().save(filename)
  }

  /**
    * Loads a NaiveBayesTagger from a provided filename.
    *
    * @param filename The filename to load from.
    * @return The loaded Tagger.
    */
  def load(filename: String): NaiveBayesTagger = {
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

    val prefixTransformer = new SQLTransformer()
      .setStatement(s"SELECT *, prefixTokens(${tokenizer.getOutputCol}, $inputCol)" +
                    s"as prefix_${tokenizer.getOutputCol}_tokens FROM __THIS__")

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
  private def prefixTokens(tokens: Vector[String], prefix: String): Vector[String] = {
    tokens.map(prefix + _)
  }
}
