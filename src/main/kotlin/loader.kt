import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import tornadofx.*
import javafx.scene.paint.Color
import tornadofx.Stylesheet.Companion.button

class values(name: String, value: Int){
    val nameProperty = SimpleStringProperty(this, "Name", name)
    val valueProperty = SimpleIntegerProperty(this, "Value", value)
}

@ExperimentalStdlibApi
fun get_file_params(filename: String):Triple<Int, Int, Int> {
    val stream = File(filename).inputStream()
    var bytes = ByteArray(8)
    val params =  Triple(readtext_Stream(266, stream, bytes), readtext_Stream(72, stream, bytes), readtext_Stream(72, stream, bytes))
    return params
}

@ExperimentalStdlibApi
fun readtext_Stream(position: Long, stream: FileInputStream, bytes: ByteArray): Int {
    stream.skip(position)
    stream.read(bytes)
    val value = bytes.decodeToString()
    //println(value.trim())
    val final: Int = value.trim().toInt()
    return final
}

@ExperimentalStdlibApi
fun get_data(filename: String, params: Triple<Int, Int, Int>, legnth: Int): ShortBuffer {
    val DATA_OFFSET: Long = 2880
    val file_data: RandomAccessFile = RandomAccessFile(filename, "r")
    var bytes_new = ByteArray(params.first * params.second * 2 * legnth)
    file_data.seek(DATA_OFFSET)
    file_data.read(bytes_new)
    val view = ByteBuffer.wrap(bytes_new).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
    return(view)
}

class MyApp: App(MyView::class)

class MyView: View(){
        override val root = borderpane {
            top = hbox{
                label("file name"){
                    textFill = Color.AZURE
                }
            }
            left = vbox {
                button ("load")
                button ("get para")
                button ("get block data")
                button (" get block")
            }
            center = tableview<values> {
                items = listOf(
                    values(" Width", 0),
                    values("Height", 0),
                    values("number frames",0),
                values("Block Length", 10)).observable()

                column("paramater", values::nameProperty)
                column("Values", values::valueProperty)
            }
        }
    }


@ExperimentalStdlibApi
fun main() {
    launch<MyApp>()
    val profile: IntArray = IntArray(4)
    val pathname: String = "Data/KimLab_cell3-small.tsm"
    val params = get_file_params(pathname)
    println(params)
}