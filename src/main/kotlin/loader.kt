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
import javafx.stage.FileChooser


class characters(name: String, bunny: Int){
    val nameProperty = SimpleStringProperty(this, "Name", name)
    val valueProperty = SimpleIntegerProperty(this, "Value", bunny)
}

@ExperimentalStdlibApi
fun get_file_params(filename: String):Triple<Int, Int, Int> {
    val stream = File(filename).inputStream()
    val bytes = ByteArray(8)
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
fun get_data(filename: String, params: Triple<Int, Int, Int>, length: Int): ShortBuffer {
    val DATA_OFFSET: Long = 2880
    val file_data: RandomAccessFile = RandomAccessFile(filename, "r")
    val bytes_new = ByteArray(params.first * params.second * 2 * length)
    file_data.seek(DATA_OFFSET)
    file_data.read(bytes_new)
    val view = ByteBuffer.wrap(bytes_new).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
    return(view)
}

private fun getlinepro(filename: String, params: Triple<Int, Int, Int>): DoubleArray {
    val profile: DoubleArray = DoubleArray(params.third )
    val stream = File(filename).inputStream()
    var bytes = ByteArray(2)
    stream.skip(2880)
    for (x in 0 ..params.third-1){
        stream.read(bytes)
        var outer: ShortBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        profile[x] = outer.get(0).toDouble()
        stream.skip((params.first * params.second * 2).toLong()-2)
    }
    return(profile)
}


class MyApp: App(MyView::class)

class MyView: View(){
        @ExperimentalStdlibApi
        override val root = borderpane {

            val items = listOf(
                characters("Width", 0),
                characters("Height", 0),
                characters("number frames",0),
                characters("Block Length", 0)).asObservable()



            top = hbox{
                label("file name"){
                    textFill = Color.AZURE
                }
            }
            left = vbox {

              val loadbutton = button{
                  text ="Load File"
                  setMinSize(110.0, 10.0)
                  setMaxSize(110.0,100.0)

                  action {
                      val ef = arrayOf(FileChooser.ExtensionFilter("NDR Files (.tsm)", "*.tsm"))
                      val fn: List<File> = chooseFile("Select NDR file", ef)
                      val pathname: String = fn[0].toString()
                      val params = get_file_params(pathname)
                      items[0].valueProperty.set(params.first)
                      items[1].valueProperty.set(params.second)
                      items[2].valueProperty.set(params.third)
    //               if (params.third < 1000) {
    //                   column("number frames", characters::valueProperty).cellFormat {
   //               }
    //              }

                    }
                }


                button{
                    text = "Get Block Data"
                    setMinSize(110.0, 10.0)
                    setMaxSize(110.0,100.0)
                    action{
                        val profile: DoubleArray = getlinepro(pathname, params as Triple<Int, Int, Int>)
                    }
                }
                button{
                    text = "Get Block"
                    setMinSize(110.0, 10.0)
                    setMaxSize(110.0,100.0)
                }
            }
            center = tableview(items) {
                column("Parameter", characters::nameProperty)
                column("Values", characters::valueProperty)
            }
        }
    }

class ParamModel(value: characters) : ItemViewModel<characters>(value){
    val name = bind(characters::nameProperty)
    val bunny = bind(characters::valueProperty)
}

@ExperimentalStdlibApi
fun main() {
    launch<MyApp>()
//    val pathname: String = "Data/KimLab_cell3-small.tsm"
//    val params = get_file_params(pathname)
//    println(params)
}