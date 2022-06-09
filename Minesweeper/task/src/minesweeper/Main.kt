package minesweeper

import kotlin.random.Random

fun main() {

    print("How many mines do you want on the field?")
    val mineCount = readln().toInt()
    val playground = MutableList(9){ MutableList(9){"."} }

    var actualMines = 0
    while(actualMines < mineCount) {
        val i = Random.nextInt(0, 9)
        val j = Random.nextInt(0, 9)
        if (playground[i][j] != "X") {
            playground[i][j] = "X"
            actualMines++
        }
    }

    for(rowIndex in playground.indices)
        println(playground[rowIndex].joinToString(""))

}

