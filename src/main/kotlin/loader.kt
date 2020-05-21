import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import tornadofx.*

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
        override val root = vbox{
            button("Press me")
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