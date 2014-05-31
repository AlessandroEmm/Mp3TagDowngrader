package ch.ale.TagConverter

import java.nio.file.{ Paths, Files }
import java.io.File
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import scala.collection.JavaConverters._
import org.jaudiotagger.tag.id3.ID3v11Tag
import org.jaudiotagger.tag.TagField
import com.typesafe.scalalogging.slf4j.LazyLogging

object TagConverter extends LazyLogging {

  def main(args: Array[String]): Unit =
    {
      args.par.foreach { path =>
        if (Files.exists(Paths.get(path)))
          getFileTree(new File(path)).foreach { file =>
            logger.debug(file.toString)
            if (file.isFile() && file.toString.endsWith("mp3")) {
              val f = AudioFileIO.read(file).asInstanceOf[MP3File]
              downgradeMediaTag(f).save
            } else logger.debug("no Valid File")
          }

        else logger.debug(path + "is not a valid Path")

      }
    }

  def downgradeMediaTag(file: MP3File): MP3File = {
    if (!file.hasID3v1Tag && !file.hasID3v2Tag)
      file
    else if (file.hasID3v2Tag) {
      val v2tag = file.getID3v2Tag
      val v1tag = new ID3v11Tag(v2tag)

      file.setID3v1Tag(v1tag)
      file.setID3v2Tag(null)
      file
    } else
      file

  }

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
    else Stream.empty)

}