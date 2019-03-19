package ml.spark

import scala.collection.Map
import ml.{Tagger, TaggerDefinition}
import model.base.Clue
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

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
      val clueTup = Clue.unapply(clue).get
      (clueTup._1, clueTup._2, clueTup._3, clueTup._4, clueTup._5, 0)
    }.toDF("question", "answer", "category", "value", "round", "label")
    val classification = model.transform(df)

    val semcat = classification.orderBy("probability").first().getAs[Double]("prediction").toInt
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
  import spark.implicits._

  /**
    * Creates a new tagger from the provided training dataset.
    *
    * @param train The map from input to output to serve as training data.
    * @return The created Tagger from the provided training data.
    */
  override def create(train: Map[Clue, Int]): NaiveBayesTagger = {
    // Map the training data to a DataFrame.
    val df = train.toSeq.map { kvp =>
      val c = kvp._1
      (c.question, c.answer, c.category, c.value, c.round, kvp._2)
    }.toDF("question", "answer", "category", "value", "round", "label")

    val sqlTransformer = new SQLTransformer()
      .setStatement("SELECT concat_ws(\" \", question, answer, category) as combined_text, value, round, label FROM __THIS__")

    // Create my pipeline stages.
    val tokenizer = new Tokenizer()
      .setInputCol("combined_text")
      .setOutputCol("tokens")

    val counter = new CountVectorizer()
      .setInputCol("tokens")
      .setOutputCol("features")

    val naiveBayes = new NaiveBayes()
      .setModelType("multinomial")
      .setSmoothing(1)

    val classifierPipeline = new Pipeline()
      .setStages(Array(sqlTransformer, tokenizer, counter, naiveBayes))
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
}
