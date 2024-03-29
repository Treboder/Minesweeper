package minesweeper

import java.util.*
import kotlin.random.Random

fun main() {

    val playground = Playground()                       // create new playground, ask for number of mines and place the mines randomly
    val player = Player(playground)                     // create player with playground to operate on

    // play the game
    while (player.haveNotFoundAllMinesYet())
        if(player.makeNextMoveWithoutSteppingOnAMine())
            Visualizations.showPlayerMap(playground)
        else
            break   // user revealed a mine resulting in a lost game

}

class Playground(_size: Int = 9) {

    val size = _size
    val mineMap = MutableList(size){ MutableList(size){ Symbols.FREE.symbol} }          // mines (x) or free (.)
    val playerMap = MutableList(size){ MutableList(size){ Symbols.UNEXPLORED.symbol} }  // marked ("*"), explored (1-8), explored with no mines around ("/") or unexplored (".")
    val neighborMap = MutableList(size){ MutableList(size) {""} }                       // filled with numbers showing the sum of neighboring mines

    init {
        print("How many mines do you want on the field?")
        val minesToBePlaced = readln().toInt()
        while(countMines() < minesToBePlaced)
            addNewMineToPlaygroundRandomly()

        createMapWithSumOfSurroundingMinesForEachField()
        Visualizations.showPlayerMap(this)
    }

    private fun countMines(): Int {
        var count = 0
        for(i in 0 until size)
            for(j in 0 until size)
                if(this.mineMap[i][j] == Symbols.MINE.symbol)
                    count++
        return count
    }

    // do something

    private fun addNewMineToPlaygroundRandomly() {
        val i = Random.nextInt(0, size)
        val j = Random.nextInt(0, size)

        if (mineMap[i][j] != Symbols.MINE.symbol)
            mineMap[i][j] = Symbols.MINE.symbol
        else
            addNewMineToPlaygroundSystematically()
    }

    private fun addNewMineToPlaygroundSystematically() {
        for(rowIndex in mineMap.indices)
            for (columnIndex in mineMap[rowIndex].indices)
                if(mineMap[rowIndex][columnIndex] != Symbols.MINE.symbol)
                {
                    mineMap[rowIndex][columnIndex] = Symbols.MINE.symbol
                    return
                }
    }

    fun relocateMineRandomly(x:Int, y:Int) {
        mineMap[x][y] = Symbols.FREE.symbol
        var foundNewPlace = false
        while(!foundNewPlace) {
            val i = Random.nextInt(0, size)
            val j = Random.nextInt(0, size)
            if (mineMap[i][j] != Symbols.MINE.symbol) {
                mineMap[i][j] = Symbols.MINE.symbol
                foundNewPlace = true
            }
            else
                foundNewPlace = false // cell already contains mine
        }
    }

    fun createMapWithSumOfSurroundingMinesForEachField() {
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
    private val playground = _playground
    private var moves = 0

    fun haveNotFoundAllMinesYet(): Boolean {

        var matchingFields = 0
        for(rowIndex in playground.mineMap.indices) {
            for (columnIndex in playground.mineMap[rowIndex].indices) {
                if(playground.playerMap[rowIndex][columnIndex] == Symbols.MARK.symbol && playground.mineMap[rowIndex][columnIndex] == Symbols.MINE.symbol)
                    matchingFields++ // player marked a mine correctly
                else if(playground.playerMap[rowIndex][columnIndex] != Symbols.MARK.symbol && playground.mineMap[rowIndex][columnIndex] == Symbols.FREE.symbol)
                    matchingFields++ // player did not mark a free cell, also wins after opening all the safe cells so that only those with unexplored mines are left.
            }
        }

        // if all mines have been marked (and not more)
        if(matchingFields == playground.size*playground.size) {
            println("Congratulations! You found all the mines!")
            return false
        }

        // if not, continue searching
        return true
    }

    fun makeNextMoveWithoutSteppingOnAMine(): Boolean {
        print("Set/unset mines marks or claim a cell as free: >  ")
        val input = Scanner(System.`in`)
        val x = input.nextInt() -1
        val y = input.nextInt() -1
        val choice = input.next()

        // ToDo: enforce consistent use of coordinates x, y in entire code; this should be better in production ;-)

        var continueGame = true
        if(choice == "mine" )
            setOrDeleteMineMark(y,x)
        else if (choice == "free" )
            continueGame = revealTheNextUnexploredField(y,x)

        return continueGame // true to continue or false if mine has been revealed
    }

    private fun setOrDeleteMineMark(x:Int, y:Int) {

        if (playground.playerMap[x][y] == Symbols.MARK.symbol)
            playground.playerMap[x][y] = Symbols.FREE.symbol

        else if(playground.playerMap[x][y] == Symbols.FREE.symbol)
            playground.playerMap[x][y] = Symbols.MARK.symbol

    }

    private fun revealTheNextUnexploredField(x:Int, y:Int): Boolean {

        // relocate the mine at x;y if this is the very first exploring move
        if(++moves == 1 && playground.mineMap[x][y] == Symbols.MINE.symbol)
        {
            playground.relocateMineRandomly(x,y)
            playground.createMapWithSumOfSurroundingMinesForEachField()
        }

        // check for mines
        if(playground.mineMap[x][y] == Symbols.MINE.symbol) {
            Visualizations.includeAllMinesOnPlayerMap(playground)
            Visualizations.showPlayerMap(playground)
            println("You stepped on a mine and failed!")
            return false // user revealed a mine resulting in a lost game
        }
        // reveal the field with the number of mines in the neighbourhood, if there are
        else if (playground.neighborMap[x][y].toInt() > 0) {
            playground.playerMap[x][y] = playground.neighborMap[x][y]
        }
        // in case of zero mines in the neighbourhood, reveal iteratively all connecting fields with zero neighboring mines
        else if (playground.neighborMap[x][y].toInt() == 0) {
            playground.playerMap[x][y] = Symbols.EXPLORED_WITHOUT_MINES_AROUND.symbol
            revealAllFieldsConnectedToExploredFieldsWithZeroMinesInTheirNeighbourhood()
            removeImpossibleMarksAfterAutomaticallyRevealedFields()
        }

        return true
    }

    private fun revealAllFieldsConnectedToExploredFieldsWithZeroMinesInTheirNeighbourhood() {

        var foundOne = true
        while(foundOne) {
            foundOne = false
            for(rowIndex in playground.mineMap.indices) {
                for (columnIndex in playground.mineMap[rowIndex].indices) {
                    if(playground.playerMap[rowIndex][columnIndex] == Symbols.UNEXPLORED.symbol && isNextToAtLeastOneExploredFieldWithoutSurroundingMines(rowIndex, columnIndex)) {
                        foundOne = true
                        if(playground.neighborMap[rowIndex][columnIndex].toInt() == 0)
                            playground.playerMap[rowIndex][columnIndex] = Symbols.EXPLORED_WITHOUT_MINES_AROUND.symbol
                        else
                            playground.playerMap[rowIndex][columnIndex] = playground.neighborMap[rowIndex][columnIndex]
                    }
                }
            }
        }
    }

    private fun isNextToAtLeastOneExploredFieldWithoutSurroundingMines(i:Int, j:Int): Boolean {

        for(x in i-1..i+1)
            for(y in j-1..j+1)
                if(x<0 || x>=playground.size)
                    continue
                else if(y<0 || y>=playground.size)
                    continue
                else if(x==i && y==j)
                    continue
                else if(playground.playerMap[x][y] == Symbols.EXPLORED_WITHOUT_MINES_AROUND.symbol)
                    return true
                else if(playground.playerMap[x][y] == Symbols.MARK.symbol && playground.neighborMap[x][y].toInt() == 0)
                    return true

        return false
    }

    private fun removeImpossibleMarksAfterAutomaticallyRevealedFields() {
        var foundOne = true
        while(foundOne) {
            foundOne = false
            for(rowIndex in playground.mineMap.indices) {
                for (columnIndex in playground.mineMap[rowIndex].indices) {
                    if(playground.playerMap[rowIndex][columnIndex] == Symbols.MARK.symbol && isNextToAtLeastOneExploredFieldWithoutSurroundingMines(rowIndex, columnIndex)) {
                        foundOne = true
                        if(playground.neighborMap[rowIndex][columnIndex].toInt() == 0)
                            playground.playerMap[rowIndex][columnIndex] = Symbols.EXPLORED_WITHOUT_MINES_AROUND.symbol
                        else
                            playground.playerMap[rowIndex][columnIndex] = playground.neighborMap[rowIndex][columnIndex]

                    }
                }
            }
        }
    }
}

class Visualizations {

    companion object {

        fun showPlayerMap(playground: Playground) {
            println("")
            //println("-|mineMap|")
            println(" |123456789|")
            println("-|---------|")
            for(rowIndex in playground.mineMap.indices)
                println("${rowIndex+1}|" + playground.playerMap[rowIndex].joinToString("") + "|")
            println("-|---------|")
        }

        fun includeAllMinesOnPlayerMap(playground: Playground) {
            for(rowIndex in playground.mineMap.indices)
                for (columnIndex in playground.mineMap[rowIndex].indices)
                    if(playground.mineMap[rowIndex][columnIndex] == Symbols.MINE.symbol)
                        playground.playerMap[rowIndex][columnIndex] = Symbols.MINE.symbol
        }
    }
}

enum class Symbols (val symbol:String) {
    MINE("X"),
    FREE("."),
    MARK("*"),
    UNEXPLORED("."),
    EXPLORED_WITHOUT_MINES_AROUND("/")
}
