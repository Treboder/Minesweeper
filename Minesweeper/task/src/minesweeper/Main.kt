package minesweeper

import kotlin.random.Random

fun main() {

    print("How many mines do you want on the field?")
    val initialMineCount = readln().toInt()

    val playground = Playground()
    while(playground.mineCount() < initialMineCount)
        playground.addMineRandomly()

    for(rowIndex in playground.fields.indices)
        println(playground.fields[rowIndex].joinToString(""))

}

class Playground(_size: Int = 9) {
    val size = _size
    val fields = MutableList(size){ MutableList(size){"."} }

    fun mineCount(): Int {
        var count = 0
        for(i in 0..size-1)
            for(j in 0..size-1)
                if(this.fields[i][j] == "X")
                    count++
        return count
    }

    fun addMineRandomly(): Boolean {
        val i = Random.nextInt(0, size)
        val j = Random.nextInt(0, size)
        if (fields[i][j] != "X")
            fields[i][j] = "X"
        else
            return false
        return true
    }
}

