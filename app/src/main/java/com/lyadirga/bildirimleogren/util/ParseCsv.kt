package com.lyadirga.bildirimleogren.util

import android.content.Context
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.model.LearningSet
import com.lyadirga.bildirimleogren.model.Sentence
import java.io.BufferedReader
import java.io.InputStreamReader

class ParseCsv {
    companion object{
        fun parse(context: Context): LearningSet{
            val set = LearningSet()
            val inputStream = context.resources.openRawResource(R.raw.eeee)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            var isHeaderSet = false
            while (reader.readLine().also { line = it } != null) {
                if (isHeaderSet){
                    val list = parseCsvLine(line!!)
                    if (list.size >= 2) {
                        val sentence = Sentence(list[0], list[1])
                        set.data.add(sentence)
                    }
                }else{
                    set.header = line.toString().dropLast(1)
                    isHeaderSet = true
                }
            }

            inputStream.close()
            reader.close()
            return set
        }

        private fun parseCsvLine(line: String): List<String> {
            val result = mutableListOf<String>()
            var insideQuotes = false
            val builder = StringBuilder()

            for (char in line) {
                when {
                    char == '"' -> insideQuotes = !insideQuotes
                    char == ',' && !insideQuotes -> {
                        result.add(builder.toString())
                        builder.clear()
                    }
                    else -> builder.append(char)
                }
            }

            result.add(builder.toString()) // Son sütunu ekleyin

            return result
        }
    }
}