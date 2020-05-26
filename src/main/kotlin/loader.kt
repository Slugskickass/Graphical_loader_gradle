import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt


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

fun diffdata(data: DoubleArray): DoubleArray{
    val len = data.size
    val newarray: DoubleArray = DoubleArray(len)
    for (i in 0 until len-1){
        newarray[i] = (data[i+1] - data[i]).absoluteValue
    }
    return newarray
}

fun calculateSD(numArray: DoubleArray): Double {
    var sum = 0.0
    var standardDeviation = 0.0
    for (num in numArray) {
        sum += num
    }
    val mean = sum / numArray.size
    for (num in numArray) {
        standardDeviation += (num - mean).pow(2.0)
    }
    return sqrt(standardDeviation / numArray.size)
}

fun getBlockParams(diff_data: DoubleArray, std_dev: Double):Triple<Int,Int, Int>{

    var temp_store: DoubleArray = doubleArrayOf(0.0,0.0,0.0) //,0.0,0.0,0.0,0.0,0.0,0.0,0.0)
    var count = 0
    for (I in 0..diff_data.size){
        if (diff_data[I] > std_dev * 5){
            temp_store[count] = I.toDouble()
            count += 1
            if (count == temp_store.size){
                break
            }
        }
    }

    val differences: DoubleArray = diffdata(temp_store)
    val number_blocks =((diff_data.size - temp_store[0]) / differences.average()).toInt()
    val block_params= Triple(temp_store[0].toInt(), differences.average().toInt(), number_blocks)
    return block_params
}

class MyApp: App(MyView::class)

class MyView: View(){

    var pathname: String = " "
    var file_params = Triple(0,0,0)
    var profile: DoubleArray = DoubleArray(10)
    var block_params = Triple(0,0, 0)
    val result = SimpleStringProperty()
    @ExperimentalStdlibApi
        override val root = borderpane {

            val items = listOf(
                characters("Width", 0),
                characters("Height", 0),
                characters("number frames",0),
                characters("Block Start", 0),
                characters("Block Length", 0),
                characters("Number of Blocks", 0),
                characters("Standard Deviation", 0)).asObservable()



            top = hbox {
                val file_name_show = label("file name") {
                    textFill = Color.BLACK
                    label().bind(result)
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
                      pathname = fn[0].toString()
                      file_params = get_file_params(pathname)
                      items[0].valueProperty.set(file_params.first)
                      items[1].valueProperty.set(file_params.second)
                      items[2].valueProperty.set(file_params.third)
                      result.value = pathname
                    }
                }


                button{
                    text = "Get Block Data"
                    setMinSize(110.0, 10.0)
                    setMaxSize(110.0,100.0)
                    action{
                    //    val profile: DoubleArray = getlinepro(pathname, file_params as Triple<Int, Int, Int>)
                    profile = getlinepro(pathname, file_params as Triple<Int, Int, Int>)
                    val diff_profile = diffdata(profile)
                    val std_devi = calculateSD(diff_profile)
                    block_params = getBlockParams(diff_profile, std_devi)
                    items[3].valueProperty.set(block_params.first)
                    items[4].valueProperty.set(block_params.second)
                    items[5].valueProperty.set(block_params.third)
                    items[6].valueProperty.set(std_devi.toInt())
                    }
                }

                button{
                    text = "Plot"
                    setMinSize(110.0, 10.0)
                    setMaxSize(110.0,100.0)
                    action {

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
 //           bottom = vbox{
 //               val sctter = scatterchart("Line profile of Block", NumberAxis(), NumberAxis()) {
 //                   series("Profile") {
//
//                    }
//                }
//            }
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