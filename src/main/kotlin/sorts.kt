import java.io.File
import java.time.Duration
import java.time.LocalTime


//Быстрая (рекурсивная реализация, разбиение Ломуто, опорный - последний)
//Двухпутевые вставки
// по убыв, по возраст
class Passport(series: Int, number: Int) {
    init {
        if (!(1..9999).contains(series) && !(1..999999).contains(number)) throw Exception()
    }

    var series: Int = series % 10000
    var number: Int = number % 1000000

    operator fun compareTo(other: Passport): Int {
        val result = when {
            this.series > other.series -> -1
            this.series < other.series -> 1
            this.number < other.number -> -1
            this.number > other.number -> 1
            else -> 0
        }
        return result
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Passport) return false
        return this.series == other.series && this.number == other.number
    }

    override fun toString(): String {
        return "${this.series} ${this.number}"
    }
}

fun quickSort(array: Array<Passport>) {
    quickSortRec(array, 0, array.size - 1)
}

fun quickSortRec(array: Array<Passport>, left: Int, right: Int) {
    if (left < right) {
        val pivot = partition(array, left, right)
        quickSortRec(array, left, pivot - 1)
        quickSortRec(array, pivot + 1, right)
    }
}

fun partition(array: Array<Passport>, left: Int, right: Int): Int {
    val pivot = array[right]
    var i = left - 1
    for (j in left..<right) {
        if (array[j] < pivot) {
            i++
            swap(array, i, j)
        }
    }
    swap(array, i + 1, right)
    return i + 1
}

fun swap(array: Array<Passport>, i: Int, j: Int) {
    val temp = array[i]
    array[i] = array[j]
    array[j] = temp
}

fun twoWayInsertionSort(arr: Array<Passport>) {
    val n = arr.size
    if (n < 2) return

    val deque = ArrayDeque<Passport>(n)
    deque.addFirst(arr[0])

    for (i in 1..<n) {
        if (arr[i] < deque.first()) {
            deque.addFirst(arr[i])
        } else if (arr[i] > deque.last()) {
            deque.addLast(arr[i])
        } else {
            val tempList = deque.toList().toMutableList()
            var j = 0
            while (j < tempList.size && tempList[j] < arr[i]) {
                j++
            }
            tempList.add(j, arr[i])
            deque.clear()
            deque.addAll(tempList)
        }
    }
    arr.forEachIndexed { index, _ ->
        arr[index] = deque.removeFirst()
    }
}

fun fillFile(path: String, size: Int) {
    val file = File(path)
    val isNewFileCreated = file.createNewFile()
    if (isNewFileCreated) {
        for (i in 1..size) {
            val series = (1..9999).random()
            val number = (1..999999).random()
            file.appendText("$series $number\n")
        }
    }
}

fun initFromFile(path: String): Array<Passport> {
    val array: MutableList<Passport> = mutableListOf()
    File(path).bufferedReader().readLines().forEach { s ->
        try {
            val (series, number) = s.split(" ").map { it.toInt() }
            array.add(Passport(series, number))
        } catch (e: Exception) {
            println("$s is not valid passport")
        }
    }
    return array.toTypedArray()
}

fun countTimeAndCreateFile(func: (Array<Passport>) -> Unit, array: Array<Passport>, fileName: String) {
    val start = LocalTime.now()
    func(array)
    val end = LocalTime.now()
    val file = File(fileName)
    val isNewFileCreated: Boolean = file.createNewFile()
    if (isNewFileCreated) {
        array.forEach { file.appendText("$it\n") }
        file.appendText("Time: ${Duration.between(start, end).toMillis()}\n")
    }
}

fun main() {
    fillFile("src/main/kotlin/passport_db.txt", 500000)
    val array1 = initFromFile("src/main/kotlin/passport_db.txt")
    val array2 = initFromFile("src/main/kotlin/passport_db.txt")
    countTimeAndCreateFile(::quickSort, array1, "src/main/kotlin/quickSort.txt")
    countTimeAndCreateFile(::twoWayInsertionSort, array2, "src/main/kotlin/twoInsertSort.txt")
}