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

    // handle first move, which is special in a sense that it must not explore a mine by accident

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
       /* mineMap[1][4] = Symbols.MINE.symbol
        mineMap[2][5] = Symbols.MINE.symbol
        mineMap[5][4] = Symbols.MINE.symbol
        mineMap[7][6] = Symbols.MINE.symbol
        */


        print("How many mines do you want on the field?")
        val minesToBePlaced = readln().toInt()
        while(countMines() < minesToBePlaced)
            addNewMineToPlaygroundRandomly()

        createMapWithSumOfSurroundingMinesForEachField()
        Visualizations.showPlayerMap(this)
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
                foundNewPlace = false // cell already contains a mine
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
    val playground = _playground
    var moves = 0

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

        else if(playground.playerMap[x][y] == Symbols.FREE.symbol)
            playground.playerMap[x][y] = Symbols.MARK.symbol

        //Visualizations.showPlayerMap(playground)
    }

    fun revealTheNextUnexploredField(x:Int, y:Int): Boolean {

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

    fun revealAllFieldsConnectedToExploredFieldsWithZeroMinesInTheirNeighbourhood() {

        var foundOne = true
        while(foundOne) {
            foundOne = false
            for(rowIndex in playground.mineMap.indices) {
                for (columnIndex in playground.mineMap[rowIndex].indices) {

                    if(rowIndex == 8 && columnIndex == 8) {
                        val v1 = playground.playerMap[rowIndex][columnIndex]
                        val v2 = isNextToAtLeastOneExploredFieldWithoutSurroundingMines(rowIndex, columnIndex)
                        val v3 = playground.neighborMap[rowIndex][columnIndex].toInt()
                        val v4 = playground.playerMap[7][8]
                        val v5 = playground.playerMap[8][7]
                        true
                    }

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

    fun isNextToAtLeastOneExploredFieldWithoutSurroundingMines(i:Int, j:Int): Boolean {

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

    fun removeImpossibleMarksAfterAutomaticallyRevealedFields() {
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
