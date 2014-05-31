package ch.ale.barcode

import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common._
import javax.imageio.ImageIO
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.Paths
import java.io.File
import scala.collection.JavaConversions._
import java.nio.file.WatchEvent
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import com.google.zxing.oned.Code128Reader
import com.google.zxing.BinaryBitmap
import java.awt.image.BufferedImage
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.Binarizer
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.apache.pdfbox.rendering.PDFRenderer
import com.google.zxing.oned.ITFReader
import com.google.zxing.MultiFormatReader
import com.google.zxing.multi.GenericMultipleBarcodeReader
import java.util.EnumMap
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import java.lang.Boolean

object Scan {

  val rootpath = "/tmp/test/"

  def main(args: Array[String]): Unit = {

    val stopper = new AtomicBoolean
    stopper.set(true)

    val folder = Paths.get(new File(rootpath).toURI())
    // Create a new Watch Service
    val watchService = FileSystems.getDefault().newWatchService()

    // Register events
    folder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)

    while (stopper.get()) {

      val events = watchService.take()
      events.pollEvents.foreach { x: WatchEvent[_] =>
        val z = x.asInstanceOf[WatchEvent[Path]]
        val path = z.context
        if (path.toString.endsWith(".pdf")) {
          println(rootpath + path)
          doit(new File(rootpath + path))
        } else
          println(path + " is no PDF ")

      }
      events.reset()
      // key value can be null if no event was triggered
    }

    watchService.close();
  }

  def doit(file: File): Unit = {
    def loop(in: Seq[Option[String]], out: Seq[(String, Int)], increment: Int, code: String):Seq[(String, Int)] = {
      if(in.isEmpty)  out
      else if (out.isEmpty && increment == 0) { 
        in.head match {
          case Some(code) => loop(in.tail, Seq(), 1, code)
          case None => Seq()
      }
        }
      else {
        in.head match {
          case Some(newCode) => loop(in.tail, out ++ Seq((code, increment)), 1, newCode)
          case None => {
            loop(in.tail, out, increment + 1, code)
          }
        }
        
      }
      
    }
    
    val doc = Document(file)
    println("HHHHHH")
    val codes = doc.pages.map { pic =>
      OCR.grabCode(pic) match {
        case Some(code) => code
        case None =>
      }
     
      
    }

  }
}

object OCR {

  def hints = {
    val l = new java.util.Hashtable[DecodeHintType, AnyRef]
    l.put(DecodeHintType.TRY_HARDER, Boolean.TRUE)
    l.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE)
    l
  }


  def grabCode(bi: BufferedImage) = {
    val bils = new BufferedImageLuminanceSource(bi)
    val binarizer = new HybridBinarizer(bils)
    val bb = new BinaryBitmap(binarizer)
    val reader = new ITFReader()
    val result = Try { reader.decode(bb, hints) }
    result match {
      case Success(x) => {
        println(x.getText())
        Some(x)
      }
      case Failure(e) => {
        println(e.toString)
        None
      }
    }

  }

}

object DocumentSplitter {

  import org.apache.pdfbox.util.Splitter

  def apply(file: File) = new ScanRun(file)

  class ScanRun(file: File) {
    val pages = Try {
      PDDocument.load(file)
    } match {
      case Success(document) => {
        println((document.getNumberOfPages() - 1))

        val image = 0.until((document.getNumberOfPages())).map { x =>
          val r = new PDFRenderer(document).renderImage(x, 2.5f)
          val outputfile = new File("/tmp/image" + x + ".jpg");
          ImageIO.write(r, "jpg", outputfile);
          r
        }.toSeq
        val doc = (new Splitter).split(document).map(_.getDocumentCatalog().getAllPages().get(0).asInstanceOf[PDPage]).toSeq
        document.close()
        image
      }
      case Failure(e) => {
        println("FAIL")
        Seq()
      }

    }

    println(pageCount + " This is the amount of Pages")

    def pageCount = pages.count(x => true)

    def seperated(groups: Set[Set[Int]]) = groups.map {
      group =>
        val doc = new PDDocument
        group.foreach { page =>
          //doc.addPage(pageList (page))
        }
        doc

    }
  }
}

case class Document(name: String, pages: Int, doc: PDDocument, code: Double) 