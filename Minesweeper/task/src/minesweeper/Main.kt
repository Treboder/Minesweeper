package minesweeper

import java.util.*
import kotlin.random.Random

enum class Symbols (val symbol:String) {
    MINE("X"),
    FREE("."),
    MARK("*")
}

fun main() {

    print("How many mines do you want on the field?")
    val initialMineCount = readln().toInt()

    // create playground and place the mines randomly
    val playground = Playground(initialMineCount)
    while(playground.mineCount() < initialMineCount)
        playground.addMineRandomly()

    // check neighbors and update playground with latest neighbormap
    playground.createNeighborMap()

    // show types of maps
    //playground.showMineMap()
    //playground.showNeighborMap()
    //playground.showMarkMap()
    playground.showVisiblemap()

    while (playground.userHaveNotFoundAllMinesYet())
    {
        if(playground.SetDeleteMinesMarks()) {
            playground.showVisiblemap()
            //playground.showMineMap()
            //playground.showNeighborMap()
        }

    }



}

class Playground(_mineCount: Int, _size: Int = 9) {
    val mineCount = _mineCount
    val size = _size
    val mineMap = MutableList(size){ MutableList(size){ Symbols.FREE.symbol} }          // Mines (x) or Free (.)
    val neighborMap = MutableList(size){ MutableList(size){ Symbols.FREE.symbol} }      // filled with numbers showing the sum of neighboring mines
    val markMap = MutableList(size){ MutableList(size){ Symbols.FREE.symbol} }          // Mark (*) or Free (.)

    fun mineCount(): Int {
        var count = 0
        for(i in 0..size-1)
            for(j in 0..size-1)
                if(this.mineMap[i][j] == Symbols.MINE.symbol)
                    count++
        return count
    }

    fun addMineRandomly(): Boolean {
        val i = Random.nextInt(0, size)
        val j = Random.nextInt(0, size)
        if (mineMap[i][j] != Symbols.MINE.symbol)
            mineMap[i][j] = Symbols.MINE.symbol
        else
            return false
        return true
    }

    fun calculateSurroundingMines(i: Int, j:Int): Int {
        var mineCount = 0
        for(x in i-1..i+1)
            for(y in j-1..j+1)
            {
                if(x<0 || x>=size)
                    continue;
                else if(y<0 || y>=size)
                    continue
                else if(x==i && y==j)
                    continue
                else
                {
                    if(mineMap[x][y] == Symbols.MINE.symbol)
                        mineCount++
                }
            }
        return mineCount
    }

    fun createNeighborMap() {
        for(rowIndex in this.mineMap.indices)
            for(columnIndex in this.mineMap[rowIndex].indices)
                this.neighborMap[rowIndex][columnIndex] = this.calculateSurroundingMines(rowIndex, columnIndex).toString()
    }

    fun showNeighborMap() {
        println("")
        println("-|neighborMap|")
        for(rowIndex in this.mineMap.indices) {
            for (columnIndex in this.mineMap[rowIndex].indices) {
                print("$rowIndex:$columnIndex  --> ${this.neighborMap[rowIndex][columnIndex]} | ")
            }
            println()
        }
    }

    fun showMineMap() {
        println("")
        println("-|mineMap|")
        println(" |123456789|")
        println("-|---------|")
        for(rowIndex in this.mineMap.indices)
            println("${rowIndex+1}|" + this.mineMap[rowIndex].joinToString("") + "|")
        println("-|---------|")
    }

    fun showMarkMap() {
        println("")
        println("-|markMap|")
        println(" |123456789|")
        println("-|---------|")
        for(rowIndex in this.markMap.indices)
            println("${rowIndex+1}|" + this.markMap[rowIndex].joinToString("") + "|")
        println("-|---------|")
    }

    fun showVisiblemap() {

        val visibleMap = MutableList(size){ MutableList(size){ Symbols.FREE.symbol} }          // Mark (*) or Free (.)

        for(rowIndex in this.mineMap.indices) {
            for (columnIndex in this.mineMap[rowIndex].indices) {

                if(markMap[rowIndex][columnIndex] == Symbols.MARK.symbol)
                    visibleMap[rowIndex][columnIndex] = markMap[rowIndex][columnIndex]

                else if(neighborMap[rowIndex][columnIndex].toInt() > 0 && mineMap[rowIndex][columnIndex] != Symbols.MINE.symbol)
                    visibleMap[rowIndex][columnIndex] = neighborMap[rowIndex][columnIndex]

                else
                    visibleMap[rowIndex][columnIndex] = Symbols.FREE.symbol
            }
        }

        println("")
        //println("-|visibleMap|")
        println(" |123456789|")
        println("-|---------|")
        for(rowIndex in this.mineMap.indices)
            println("${rowIndex+1}|" + visibleMap[rowIndex].joinToString("") + "|")
        println("-|---------|")

    }

    fun SetDeleteMinesMarks(): Boolean {
        print("Set/delete mines marks (x and y coordinates): > ")
        val input = Scanner(System.`in`)
        val y = input.nextInt() -1
        val x = input.nextInt() -1
        // switching the coordinates

        // ToDO: check if "There is a number here!"
        if(neighborMap[x][y].toInt() > 0 && mineMap[x][y] != Symbols.MINE.symbol)
        {
            println("There is a number here!")
            return false
        }

        if (markMap[x][y] == Symbols.MARK.symbol)
            markMap[x][y] = Symbols.FREE.symbol
        else
            markMap[x][y] = Symbols.MARK.symbol

        return true
    }

    fun userHaveNotFoundAllMinesYet(): Boolean {

        var matchingFields = 0
        for(rowIndex in this.mineMap.indices) {
            for (columnIndex in this.mineMap[rowIndex].indices) {
                if(mineMap[rowIndex][columnIndex] == Symbols.MINE.symbol && markMap[rowIndex][columnIndex] == Symbols.MARK.symbol)
                    matchingFields++
                else if(mineMap[rowIndex][columnIndex] == markMap[rowIndex][columnIndex] ) // both have "."
                    matchingFields++
            }
        }

        // if all mines have been marked (and not more)
        if(matchingFields == this.size*this.size) {
            println("Congratulations! You found all the mines!")
            return false
        }

        // otherwise continue searching
        return true
    }







}

