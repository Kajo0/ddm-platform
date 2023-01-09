package pl.edu.pw.ddm.platform.strategies.conceptdrift

import com.google.common.primitives.Doubles
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtilities
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.function.NormalDistributionFunction2D
import org.jfree.data.general.DatasetUtilities
import org.jfree.data.statistics.HistogramDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection

import java.nio.file.Files
import java.nio.file.Path

trait LocalChartDebugPrinter {

    static def DEBUG_PATH = '/tmp/TODO/FIXME/plots/'

    static def printXYChart(def splits, def x, def y) {
        def dataset = new XYSeriesCollection()

        splits.each { splitK, idValuePairs ->
            def serie = new XYSeries("split ${splitK}")

            idValuePairs.each {
                def xVal = it.values[x]
                def yVal = it.values[y]
                try {
                    serie.add(Double.parseDouble(xVal), Double.parseDouble(yVal))
                } catch (NumberFormatException e) {
                }
            }

            dataset.addSeries(serie)
        }

        def chart = ChartFactory.createScatterPlot("X(${x}) Y(${y})",
                "x (${x})",
                "y (${y})",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                true)
        chart.getXYPlot().getDomainAxis().setRange(4000, 6000)
        chart.getXYPlot().getRangeAxis().setRange(0, 10000)

        Files.createDirectories(Path.of(DEBUG_PATH))
        ChartUtilities.saveChartAsPNG(new File("${DEBUG_PATH}distribution_${x}-${y}.png"), chart, 600, 400)
        true
    }

    static def printChartEvery(def splits, def attrAmount) {
        (0..attrAmount - 1).forEach { printChart(splits, it) }
        true
    }

    static def printChart(HashMap<Integer, List<IdValuesPair>> splits, int attr) {
        def histogramDataset = new HistogramDataset()
        def lineDataset = new XYSeriesCollection()

        splits.each { splitK, idValuePairs ->
            def stats = new SummaryStatistics()
            def values = new double[idValuePairs.size()]
            def i = 0
            idValuePairs.collect { it.values[attr] }
                    .findAll { Doubles.tryParse(it) != null }
                    .collect { Double.valueOf(it) }
                    .each { stats.addValue(it) }
                    .each { values[i++] = it }

            def n1 = new NormalDistributionFunction2D(stats.getMean(), Math.max(stats.getStandardDeviation(), 0.00001))
            def s1 = DatasetUtilities.sampleFunction2DToSeries(n1, stats.getMin(), Math.max(stats.getMax(), stats.getMax() + 0.00001), (int) stats.getN() as int, "split " + splitK)
            lineDataset.addSeries(s1)

            histogramDataset.addSeries("split ${splitK}", values, 50)
        }

        def histChart = ChartFactory.createHistogram("Histogram ${attr}",
                'Data',
                'Frequency',
                histogramDataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                true)
        def lineChart = ChartFactory.createXYLineChart("Line distribution ${attr}",
                'Data',
                'Density',
                lineDataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                true)

        Files.createDirectories(Path.of(DEBUG_PATH))
        ChartUtilities.saveChartAsPNG(new File("${DEBUG_PATH}histogram_${attr}.png"), histChart, 600, 400)
        ChartUtilities.saveChartAsPNG(new File("${DEBUG_PATH}distribution_${attr}.png"), lineChart, 600, 400)

        true
    }

}
