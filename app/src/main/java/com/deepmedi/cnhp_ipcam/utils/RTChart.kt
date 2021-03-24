package com.deepmedi.cnhp_ipcam.utils

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

object RTChart {

    fun setLineChart(lineChart: LineChart, name:String, maxNum: Int){
        lineChart.also {
            it.setDrawGridBackground(true)
            it.setBackgroundColor(Color.TRANSPARENT)
            it.setGridBackgroundColor(Color.TRANSPARENT)

            it.setTouchEnabled(false)
            it.isDragEnabled = false
            it.setScaleEnabled(false)
            it.setPinchZoom(false)
            it.isAutoScaleMinMaxEnabled = true

            it.xAxis.run {
                setDrawGridLines(false)
                setDrawAxisLine(false)
                setDrawGridLines(false)
                isEnabled = false
            }

            it.axisLeft.run{
                setDrawGridLines(false)
                setDrawAxisLine(false)
                setDrawGridLines(false)
                isEnabled = false
            }

            it.axisRight.run{
                setDrawGridLines(false)
                setDrawAxisLine(false)
                setDrawGridLines(false)
                isEnabled = false
            }

//            it.setVisibleXRangeMaximum(maxNum.toFloat())

            it.legend.isEnabled = false
            it.description.text = name

            it.invalidate()
        }
    }

    fun addEntryVal(lineChart: LineChart, value:Float,colorType: Int, maxNum:Int){
        lineChart.run{

            if(data == null){
                data = LineData()
            }

            data.run{
                var set = getDataSetByIndex(0)
                if(set == null){
                    set = createSet(colorType)
                    addDataSet(set)
                }
                addEntry(Entry(set.entryCount.toFloat(), value), 0)
                notifyDataChanged()
            }
            notifyDataSetChanged()
            setVisibleXRangeMaximum(maxNum.toFloat())
            moveViewTo(data.entryCount.toFloat(), maxNum.toFloat(), YAxis.AxisDependency.LEFT)
        }
    }

    fun addEntryMtx(lineChart: LineChart, values:FloatArray, colorType: Int, maxNum: Int){
        lineChart.run{

            if(data == null){
                data = LineData()
            }

            data.run{
                var set = getDataSetByIndex(0)
                if(set == null){
                    set = createSet(colorType)
                    addDataSet(set)
                }
                values.forEach {
                    addEntry(Entry(set.entryCount.toFloat(), it), 0)
                }
                notifyDataChanged()
            }
            notifyDataSetChanged()
            setVisibleXRangeMaximum(maxNum.toFloat())
            moveViewTo(data.entryCount.toFloat(), maxNum.toFloat(), YAxis.AxisDependency.LEFT)
        }
    }

    fun clearData(lineChart: LineChart){
        lineChart.invalidate()
        lineChart.lineData.clearValues()
    }

    private fun createSet(colorType:Int): LineDataSet {
        return LineDataSet(null, "").apply{
            lineWidth = 2f
            color = colorType
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircles(false)
            setDrawValues(false)
        }
    }


}