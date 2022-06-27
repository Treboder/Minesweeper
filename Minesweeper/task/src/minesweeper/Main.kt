package minesweeper

import java.util.*
import kotlin.random.Random

enum class Symbols (val symbol:String) {
    MINE("X"),
    FREE("."),
    MARK("*"),
    UNEXPLORED("."),
    EXPLORED_WITHOUT_MINES_AROUND("/")
}

fun main() {

    val playground = Playground()                       // create new playground, ask for number of mines and place the mines randomly
    val player = Player(playground)                     // create player with playground to operate on

    // play the game
    while (player.haveNotFoundAllMinesYet())
        if(player.makeNextMoveWithoutSteppingOnAMine())
            Visualizations.showVisiblemap(playground)
        else
            break   // user revealed a mine resulting in a lost game

}

class Playground(_size: Int = 9) {

    val size = _size                                                                    // quadratic playground
    val mineMap = MutableList(size){ MutableList(size){ Symbols.FREE.symbol} }          // Mines (x) or Free (.)
    val playerMap = MutableList(size){ MutableList(size){ Symbols.UNEXPLORED.symbol} }  // Marked (*), explored (+) or unexplored (.)
    val neighborMap = MutableList(size){ MutableList(size) {""} }                         // filled with numbers showing the sum of neighboring mines

    init {
        print("How many mines do you want on the field?")
        val minesToBePlaced = readln().toInt()
        while(countMines() < minesToBePlaced)
            addNewMineToPlaygroundRandomly()

        createMapWithSumOfSurroundingMinesForEachField()
        Visualizations.showVisiblemap(this)
    }

    private fun countMines(): Int {
        var count = 0
        for(i in 0..size-1)
            for(j in 0..size-1)
                if(this.mineMap[i][j] == Symbols.MINE.symbol)
                    count++
        return count
    }

    private fun addNewMineToPlaygroundRandomly(): Boolean {
        val i = Random.nextInt(0, size)
        val j = Random.nextInt(0, size)
        if (mineMap[i][j] != Symbols.MINE.symbol)
            mineMap[i][j] = Symbols.MINE.symbol
        else
            return false // cell already contains a mine
        return true
    }

    private fun createMapWithSumOfSurroundingMinesForEachField() {
        for(rowIndex in this.mineMap.indices)
            for(columnIndex in this.mineMap[rowIndex].indices)
                this.neighborMap[rowIndex][columnIndex] = this.calculateNumberOfSurroundingMines(rowIndex, columnIndex).toString()
    }

    private fun calculateNumberOfSurroundingMines(i: Int, j:Int): Int {
        var mineCount = 0
        for(x in i-1..i+1)
            for(y in j-1..j+1)
            {
                if(x<0 || x>=size)
                    continue
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

}

class Player(_playground: Playground) {
    val playground = _playground

    fun haveNotFoundAllMinesYet(): Boolean {

        var matchingFields = 0
        for(rowIndex in playground.mineMap.indices) {
            for (columnIndex in playground.mineMap[rowIndex].indices) {
                if(playground.mineMap[rowIndex][columnIndex] == Symbols.MINE.symbol && playground.playerMap[rowIndex][columnIndex] == Symbols.MARK.symbol)
                    matchingFields++
                else if(playground.mineMap[rowIndex][columnIndex] == playground.playerMap[rowIndex][columnIndex] ) // both have "."
                    matchingFields++
            }
        }

        // if all mines have been marked (and not more)
        if(matchingFields == playground.size*playground.size) {
            println("Congratulations! You found all the mines!")
            return false
        }

        // ToDo: Player wins after opening all the safe cells so that only those with unexplored mines are left.

        // otherwise continue searching
        return true
    }

    fun makeNextMoveWithoutSteppingOnAMine(): Boolean {
        print("Set/unset mines marks or claim a cell as free: >  ")
        val input = Scanner(System.`in`)
        val y = input.nextInt() -1  // switching the coordinates, first input is x
        val x = input.nextInt() -1  // switching the coordinates, second input is y
        val choice = input.next()

        var continueGame = true
        if(choice == "mine" )
            setOrDeleteMineMark(x, y)
        else if (choice == "free" )
            continueGame = revealTheNextUnexploredField(x,y)

        return continueGame // true to continue or false if mine has been revealed
    }

    fun setOrDeleteMineMark(x:Int, y:Int) {

        if (playground.playerMap[x][y] == Symbols.MARK.symbol)
            playground.playerMap[x][y] = Symbols.FREE.symbol
        else
            playground.playerMap[x][y] = Symbols.MARK.symbol
    }

    fun revealTheNextUnexploredField(x:Int, y:Int): Boolean {

        if(playground.mineMap[x][y] == Symbols.MINE.symbol) {
            println("You stepped on a mine and failed!")
            return false // user revealed a mine resulting in a lost game
        }
        else if (playground.neighborMap[x][y].toInt() > 0) {
            playground.playerMap[x][y] = playground.neighborMap[x][y]
        }
        else if (playground.neighborMap[x][y].toInt() == 0) {
            playground.playerMap[x][y] = Symbols.EXPLORED_WITHOUT_MINES_AROUND.symbol
            // ToDo:  no mines around, automatically reveal all the numbers in the neighbourhood automatically/iteratively
        }

        return true
    }


}

class Visualizations() {

    companion object {

        fun showNeighborMap(playground: Playground) {
            println("")
            println("-|neighborMap|")
            for(rowIndex in playground.mineMap.indices) {
                for (columnIndex in playground.mineMap[rowIndex].indices) {
                    print("$rowIndex:$columnIndex  --> ${playground.neighborMap[rowIndex][columnIndex]} | ")
                }
                println()
            }
        }

        fun showMineMap(playground: Playground) {
            println("")
            println("-|mineMap|")
            println(" |123456789|")
            println("-|---------|")
            for(rowIndex in playground.mineMap.indices)
                println("${rowIndex+1}|" + playground.mineMap[rowIndex].joinToString("") + "|")
            println("-|---------|")
        }

        fun showPlayerMap(playground: Playground) {
            println("")
            println("-|markMap|")
            println(" |123456789|")
            println("-|---------|")
            for(rowIndex in playground.playerMap.indices)
                println("${rowIndex+1}|" + playground.playerMap[rowIndex].joinToString("") + "|")
            println("-|---------|")
        }

        fun showVisiblemap(playground: Playground) {

            val visibleMap = MutableList(playground.size){ MutableList(playground.size){ Symbols.FREE.symbol} }          // Mark (*) or Free (.)

            for(rowIndex in playground.mineMap.indices) {
                for (columnIndex in playground.mineMap[rowIndex].indices) {

                    if(playground.playerMap[rowIndex][columnIndex] == Symbols.MARK.symbol)
                        visibleMap[rowIndex][columnIndex] = Symbols.MARK.symbol

                    else if(playground.neighborMap[rowIndex][columnIndex].toInt() > 0 && playground.mineMap[rowIndex][columnIndex] != Symbols.MINE.symbol)
                        visibleMap[rowIndex][columnIndex] = playground.neighborMap[rowIndex][columnIndex]

                    else
                        visibleMap[rowIndex][columnIndex] = Symbols.FREE.symbol
                }
            }

            println("")
            //println("-|visibleMap|")
            println(" |123456789|")
            println("-|---------|")
            for(rowIndex in playground.mineMap.indices)
                println("${rowIndex+1}|" + visibleMap[rowIndex].joinToString("") + "|")
            println("-|---------|")

        }
    }


}
