package ml.spark

import scala.collection.Map
import ml.{Model, ModelDefinition}
import model.base.Clue
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession

/**
  * The bare tagger functionality.
  */
class NaiveBayesTagger private (val model: PipelineModel) extends Model[Clue, String] {

  /**
    * Tag an input with a given output label.
    *
    * @param input The input clue that is to be tagged.
    * @return The semantic category label that is the result of the transformation.
    */
  def apply(input: Clue): String = {
    val spark = SparkSession.builder
      .appName("jtagger")
      .config("spark.master", "local")
      .getOrCreate()
    import spark.implicits._

    val df = Seq(input)
      .map { clue =>
        val clueTup = Clue.unapply(clue).get
        (clueTup._1, clueTup._2, clueTup._3, clueTup._4, clueTup._5, "")
      }
      .toDF("question", "answer", "category", "value", "round", "label")
    val classification = model.transform(df)

    classification
      .orderBy(NaiveBayesTagger.ProbabilityCol)
      .first()
      .getAs[String](NaiveBayesTagger.PredictionCol)
  }
}

/**
  * Definition of the NaiveBayesTagger creation and persistence and lifetime behavior.
  */
object NaiveBayesTagger extends ModelDefinition[NaiveBayesTagger, Clue, String] {
  val spark = SparkSession.builder
    .appName("jtagger")
    .config("spark.master", "local")
    .getOrCreate()
  import spark.implicits._

  /**
    * These constants define the column names for the prediction and probability info for each data item.
    */
  private val PredictionCol = "prediction"
  private val ProbabilityCol = "probability"

  /**
    * Creates a new tagger from the provided training dataset.
    *
    * @param train The map from input to output to serve as training data.
    * @return The created Tagger from the provided training data.
    */
  override def create(train: Map[Clue, String]): NaiveBayesTagger = {
    // Map the training data to a DataFrame.
    val df = train.toSeq
      .map { kvp =>
        val c = kvp._1
        (c.question, c.answer, c.category, c.value, c.round, kvp._2)
      }
      .toDF("question", "answer", "category", "value", "round", "label")

    // Create my pipeline stages.
    // Fit this separately and create a model so that it can be referenced in the IndexToString stage definition
    val indexer = new StringIndexer()
      .setInputCol("label")
      .setOutputCol("indexed_label")
      .setHandleInvalid("keep")
    val indexerModel = indexer.fit(df)

    val sqlTransformer = new SQLTransformer()
      .setStatement(
        "SELECT concat_ws(\" \", question, answer, category) as combined_text, value, round, label FROM __THIS__"
      )

    val tokenizer = new Tokenizer()
      .setInputCol("combined_text")
      .setOutputCol("tokens")

    val counter = new CountVectorizer()
      .setInputCol("tokens")
      .setOutputCol("features")

    val naiveBayes = new NaiveBayes()
      .setModelType("multinomial")
      .setSmoothing(1)
      .setFeaturesCol("features")
      .setLabelCol("indexed_label")
      .setPredictionCol("prediction_index")
      .setProbabilityCol("probability")

    val deindexer = new IndexToString()
      .setInputCol("prediction_index")
      .setOutputCol("prediction")
      .setLabels(indexerModel.labels)

    val classifierPipeline = new Pipeline()
      .setStages(Array(sqlTransformer, tokenizer, counter, indexerModel, naiveBayes, deindexer))
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
