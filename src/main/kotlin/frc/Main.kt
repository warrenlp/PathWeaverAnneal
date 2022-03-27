package frc

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors


fun findAutoPaths(basePath: String) : HashMap<Path, ArrayList<Path>> {

    val autoPath = Paths.get(basePath, "Autos")
    println("INFO: Finding auto files: $autoPath")

    var autoArray = arrayListOf<Path>()
    val autoPathArray = HashMap<Path, ArrayList<Path>>()

    if (Files.isDirectory(autoPath)) {
        val pathDirMap = Files.list(autoPath).collect(Collectors.partitioningBy { Files.isDirectory(it) })
        autoArray = pathDirMap[false] as ArrayList<Path>
    } else {
        println("WARNING: auto path is not a directory: $autoPath")
    }

    for (curAutoPath in autoArray) {
        println("INFO: Finding auto paths for auto path: $curAutoPath")
        val pathDirMap = File(curAutoPath.toString()).readLines().map { Paths.get(basePath, "Paths", it) }
        autoPathArray[curAutoPath] = pathDirMap as ArrayList<Path>
    }

    return autoPathArray
}


fun annealPathFiles(autoFilesMap: HashMap<Path, ArrayList<Path>>) {
    autoFilesMap.forEach { entry ->
        println("INFO: ${entry.key}")

        var prevPos: ArrayList<String>? = null
        entry.value.forEach { it ->
            println("INFO\t$it")
            var prevLineSplit: List<String>? = null
            val newLines = arrayListOf<String>()
            val curPathLines = File(it.toString()).readLines()
            curPathLines.forEach { it ->
                println("LINE: $it")
                val curLineSplit = it.split(",") as ArrayList<String>
                if (prevLineSplit != null) {
                    if (prevPos != null && newLines.size == 1) {
                        // Substitute previous path's last pos into new first pos
                        for (i in 0 until 2) {
                            curLineSplit[i] = prevPos!![i]
                        }
                    }

                    val newLineNumSplit = curLineSplit.slice(0..3).map {String.format("%.3f", it.toDouble())}
                    val newLineSplit = arrayListOf<String>()
                    newLineSplit.addAll(newLineNumSplit)
                    newLineSplit.addAll(curLineSplit.slice(4..curLineSplit.lastIndex))
                    newLines.add(newLineSplit.joinToString(","))

                    prevPos = curLineSplit.slice(0..1) as ArrayList<String>
                } else {
                    // Just add header line
                    newLines.add(it)
                }
                prevLineSplit = curLineSplit
            }
//            val newFileName = "$it-TEST"
            val newFileName = it.toString()
//            println("INFO: Writing file: $newFileName")
            println("INFO: Annealing file: $newFileName")

            File(newFileName).bufferedWriter().use {out ->
                out.write(newLines.joinToString("\n"))
                out.write("\n")
            }
        }
    }
}


fun main(args : Array<String>) {

    val autoFiles = findAutoPaths(args[0])

    annealPathFiles(autoFiles)

    println("INFO: All done")
}
