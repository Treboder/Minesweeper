package minesweeper

import kotlin.random.Random

const val symbolMine = "X"
const val symbolFree = "."

fun main() {

    print("How many mines do you want on the field?")
    val initialMineCount = readln().toInt()

    // create playground and place the mines randomly
    val playground = Playground()
    while(playground.mineCount() < initialMineCount)
        playground.addMineRandomly()

    // show playground
    //playground.showPlayground()
    //println("")

    // check neighbors
    playground.updateNeighborMap()
    //playground.showNeighborMap()
    //println("")

    // update playground with latest neighbormap
    playground.updatePlayground()
    playground.showPlayground()
    println("")
}

class Playground(_size: Int = 9) {
    val size = _size
    val fields = MutableList(size){ MutableList(size){ symbolFree} }
    val neighborMap = MutableList(size){ MutableList(size){ symbolFree} }

    fun mineCount(): Int {
        var count = 0
        for(i in 0..size-1)
            for(j in 0..size-1)
                if(this.fields[i][j] == symbolMine)
                    count++
        return count
    }

    fun addMineRandomly(): Boolean {
        val i = Random.nextInt(0, size)
        val j = Random.nextInt(0, size)
        if (fields[i][j] != symbolMine)
            fields[i][j] = symbolMine
        else
            return false
        return true
    }

    fun checkField(i: Int, j:Int): Int {
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
                    if(fields[x][y] == symbolMine)
                        mineCount++
                }
            }
        return mineCount
    }

    fun updateNeighborMap()
    {
        for(rowIndex in this.fields.indices)
            for(columnIndex in this.fields[rowIndex].indices)
                this.neighborMap[rowIndex][columnIndex] = this.checkField(rowIndex, columnIndex).toString()
    }

    fun showNeighborMap()
    {
        for(rowIndex in this.fields.indices) {
            for (columnIndex in this.fields[rowIndex].indices) {
                print("$rowIndex:$columnIndex  --> ${this.neighborMap[rowIndex][columnIndex]} | ")
            }
            println()
        }
    }

    fun showPlayground(){
        for(rowIndex in this.fields.indices)
            println(this.fields[rowIndex].joinToString(""))
    }

    fun updatePlayground() {
        for(rowIndex in this.fields.indices) {
            for (columnIndex in this.fields[rowIndex].indices) {
                if(fields[rowIndex][columnIndex] != symbolMine && neighborMap[rowIndex][columnIndex].toInt() > 0)
                    fields[rowIndex][columnIndex] = neighborMap[rowIndex][columnIndex]
            }
        }
    }

}

