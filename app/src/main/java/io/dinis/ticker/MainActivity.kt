package io.dinis.ticker

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate









class MainActivity : AppCompatActivity() {

    private var chart: LineChart? = null
    private var progressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar = findViewById(R.id.progressBar)

        setupChart()
        val model = ViewModelProviders.of(this).get(MainViewModel::class.java)

        model.observe(this, Observer {
            if (it!=null) render(it)
        })
    }


    private fun render(chartViewState: ChartViewState){
        if (chartViewState.isShowProgress){
            progressBar?.visibility = View.VISIBLE
        }else{
            progressBar?.visibility = View.GONE
        }

        addEntries(chartViewState.points)
    }


    private fun addEntries(list: List<Float>) {

        if (list.isEmpty()){
            return
        }

        val v = list.last()

        val data = chart?.data

        if (data != null) {

            var set: ILineDataSet? = data.getDataSetByIndex(0)

            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }

            val count = set.entryCount

            if (list.size - count != 1){
                set.clear()
                list.forEach {
                    val i = set.entryCount
                    data.addEntry(Entry(i.toFloat(), it), 0)
                }
            }else{
                data.addEntry(Entry(count.toFloat(), v), 0)
            }
            data.notifyDataChanged()



            val leftAxis = chart?.axisLeft
            val max = v + v * 0.03F
            val min = v - v * 0.03F

            leftAxis?.axisMaximum = if (count == 0) max else Math.max(max, leftAxis?.axisMaximum ?: max)
            leftAxis?.axisMinimum = if (count == 0) min else Math.max(0F,  Math.min(min, leftAxis?.axisMinimum ?: min))

            chart?.notifyDataSetChanged()
            chart?.setVisibleXRangeMaximum(120F)
            chart?.moveViewToX(data.entryCount.toFloat())
        }
    }


    private fun setupChart() {
        chart = findViewById(R.id.chart1)

        chart?.description?.isEnabled = true
        chart?.setTouchEnabled(true)
        chart?.isDragEnabled = true
        chart?.setScaleEnabled(true)
        chart?.setDrawGridBackground(false)
        chart?.setPinchZoom(true)
        chart?.setBackgroundColor(ContextCompat.getColor(this, R.color.back))

        val data = LineData()
        data.setValueTextColor(Color.BLACK)
        chart?.data = data
        val l = chart?.legend
        l?.form = LegendForm.LINE
        l?.textColor = Color.BLACK

        val xl = chart?.xAxis
        xl?.textColor = Color.BLACK
        xl?.setDrawGridLines(false)
        xl?.setAvoidFirstLastClipping(true)
        xl?.isEnabled = true

        val leftAxis = chart?.axisLeft
        leftAxis?.textColor = Color.BLACK

        leftAxis?.setDrawGridLines(true)

        val rightAxis = chart?.axisRight
        rightAxis?.isEnabled = false
    }


    private fun createSet(): LineDataSet {

        val set = LineDataSet(null, "Dynamic Data")
        set.axisDependency = AxisDependency.LEFT
        set.color = ColorTemplate.getHoloBlue()
        set.setCircleColor(Color.WHITE)
        set.lineWidth = 2f
        set.circleRadius = 4f
        set.fillAlpha = 65
        set.fillColor = ColorTemplate.getHoloBlue()
        set.highLightColor = Color.rgb(244, 117, 117)
        set.valueTextColor = Color.WHITE
        set.valueTextSize = 9f
        set.setDrawValues(false)
        return set
    }
}
