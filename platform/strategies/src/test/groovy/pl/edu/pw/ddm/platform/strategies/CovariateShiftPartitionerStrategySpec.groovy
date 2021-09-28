package pl.edu.pw.ddm.platform.strategies

import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtilities
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.function.NormalDistributionFunction2D
import org.jfree.data.general.DatasetUtilities
import org.jfree.data.statistics.HistogramDataset
import org.jfree.data.xy.XYSeriesCollection
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy
import pl.edu.pw.ddm.platform.strategies.covariateshift.CovariateShiftPartitionerStrategy
import pl.edu.pw.ddm.platform.testing.interfaces.impl.data.strategy.TempFileCreator
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class CovariateShiftPartitionerStrategySpec extends Specification {

    static def DEBUG_PATH = '/tmp/concept-drift/'

    @Shared
    def tempFileCreator = new TempFileCreator()

    @Shared
    Path sourceFile

    def setupSpec() {
        sourceFile = tempFileCreator.create('setup.txt')
        (1..50).each { sourceFile.append("$it,A,1,2,3\n") }
        (51..100).each { sourceFile.append("$it,B,2,2,3\n") }
        (101..150).each { sourceFile.append("$it,C,3,1,2\n") }
        (151..200).each { sourceFile.append("$it,D,4,4,3\n") }

        // for more data tests:
        // use imports for generator below:
        //   import de.lmu.ifi.dbs.elki.data.synthetic.bymodel.GeneratorSingleCluster
        //   import de.lmu.ifi.dbs.elki.math.statistics.distribution.NormalDistribution
//        def classes = ['A', 'B', 'C', 'D']
//        def rand = new Random()
//        (201..600).each {
//            def index = rand.nextInt(4)
//            def val = rand.nextDouble() + index
//            sourceFile.append("${it},${classes[index]},4,${val},3\n")
//        }
//
//        def size = 1000
//        def gsc = new GeneratorSingleCluster("dummy", size, 1, new Random())
//        gsc.addGenerator(new NormalDistribution(0, 10, new Random()))
//
//        for (double[] data : gsc.generate(size)) {
//            sourceFile.append("201,A,4,${data[0].intValue()},3\n")
//        }
    }

    def cleanupSpec() {
        tempFileCreator.cleanup()
    }

    def "should prepare covariate shift data scattering"() {
        setup:
        def partitioner = new CovariateShiftPartitionerStrategy()
        def dataDesc = new PartitionerStrategy.DataDesc(
                numberOfSamples: Files.lines(tempFileCreator.files.first()).count(),
                separator: ',',
                idIndex: 0,
                labelIndex: 1,
                attributesAmount: 3,
                filesLocations: [sourceFile.toString()]
        )
        def params = PartitionerStrategy.StrategyParameters.builder()
                .partitions(5)
                .seed(10)
                .customParams('shift=0.2;splits=2;method=0;attribute=1')
                .build()

        when:
        def partitions = partitioner.partition(dataDesc, params, tempFileCreator)

        then:
        Files.readAllLines(partitions[0]).stream().count() == 34
        Files.readAllLines(partitions[0]).stream().filter { it.contains('A') }.count() == 14
        Files.readAllLines(partitions[0]).stream().filter { it.contains('B') }.count() == 2
        Files.readAllLines(partitions[0]).stream().filter { it.contains('C') }.count() == 12
        Files.readAllLines(partitions[0]).stream().filter { it.contains('D') }.count() == 6
        Files.readAllLines(partitions[1]).stream().count() == 33
        Files.readAllLines(partitions[1]).stream().filter { it.contains('A') }.count() == 13
        Files.readAllLines(partitions[1]).stream().filter { it.contains('B') }.count() == 3
        Files.readAllLines(partitions[1]).stream().filter { it.contains('C') }.count() == 12
        Files.readAllLines(partitions[1]).stream().filter { it.contains('D') }.count() == 5
        Files.readAllLines(partitions[2]).stream().count() == 33
        Files.readAllLines(partitions[2]).stream().filter { it.contains('A') }.count() == 13
        Files.readAllLines(partitions[2]).stream().filter { it.contains('B') }.count() == 2
        Files.readAllLines(partitions[2]).stream().filter { it.contains('C') }.count() == 13
        Files.readAllLines(partitions[2]).stream().filter { it.contains('D') }.count() == 5
        Files.readAllLines(partitions[3]).stream().count() == 50
        Files.readAllLines(partitions[3]).stream().filter { it.contains('A') }.count() == 5
        Files.readAllLines(partitions[3]).stream().filter { it.contains('B') }.count() == 22
        Files.readAllLines(partitions[3]).stream().filter { it.contains('C') }.count() == 6
        Files.readAllLines(partitions[3]).stream().filter { it.contains('D') }.count() == 17
        Files.readAllLines(partitions[4]).stream().count() == 50
        Files.readAllLines(partitions[4]).stream().filter { it.contains('A') }.count() == 5
        Files.readAllLines(partitions[4]).stream().filter { it.contains('B') }.count() == 21
        Files.readAllLines(partitions[4]).stream().filter { it.contains('C') }.count() == 7
        Files.readAllLines(partitions[4]).stream().filter { it.contains('D') }.count() == 17

        and:
        printChart(partitioner.splitPairs)
        printSeparateChart(partitioner.splitPairs)
    }

    static def printChart(def splits) {
        def histogramDataset = new HistogramDataset()
        def lineDataset = new XYSeriesCollection()

        splits.each { splitK, idValuePairs ->
            def stats = new SummaryStatistics()
            def values = new double[idValuePairs.size()]
            def i = 0
            idValuePairs.collect { it.value }
                    .collect { Double.valueOf(it) }
                    .each { stats.addValue(it) }
                    .each { values[i++] = it }

            def n1 = new NormalDistributionFunction2D(stats.getMean(), stats.getStandardDeviation())
            def s1 = DatasetUtilities.sampleFunction2DToSeries(n1, stats.getMin(), stats.getMax(), (int) stats.getN() as int, "split " + splitK)
            lineDataset.addSeries(s1)

            histogramDataset.addSeries("split " + splitK, values, 50)
        }

        def histChart = ChartFactory.createHistogram("Histogram",
                "Data",
                "Frequency",
                histogramDataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                true)
        def lineChart = ChartFactory.createXYLineChart("Line distribution",
                "Data",
                "Density",
                lineDataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                true)

        Files.createDirectories(Path.of(DEBUG_PATH))
        ChartUtilities.saveChartAsPNG(new File("${DEBUG_PATH}histogram.png"), histChart, 600, 400)
        ChartUtilities.saveChartAsPNG(new File("${DEBUG_PATH}distribution.png"), lineChart, 600, 400)

        true
    }

    static def printSeparateChart(def splits) {
        splits.each { splitK, idValuePairs ->
            def histogramDataset = new HistogramDataset()
            def values = new double[idValuePairs.size()]
            def i = 0

            idValuePairs.collect { it.value }
                    .collect { Double.valueOf(it) }
                    .each { values[i++] = it }

            histogramDataset.addSeries("split " + splitK, values, 50)
            addSeriesBut(splits, histogramDataset, splitK)

            def chart = ChartFactory.createHistogram("Split specific histogram",
                    "Data",
                    "Frequency",
                    histogramDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    true)

            Files.createDirectories(Path.of(DEBUG_PATH))
            ChartUtilities.saveChartAsPNG(new File("${DEBUG_PATH}histogram_" + splitK + ".png"), chart, 600, 400)
        }

        true
    }

    static def addSeriesBut(def splits, def dataset, def butK) {
        splits.each { splitK, idValuePairs ->
            if (splitK == butK) {
                return
            }

            def values = new double[idValuePairs.size()]
            def i = 0
            idValuePairs.collect { it.value }
                    .collect { Double.valueOf(it) }
                    .each { values[i++] = it }

            dataset.addSeries("split $splitK", values, 50)
        }
    }

}
