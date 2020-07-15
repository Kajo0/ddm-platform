package ddm.sample

import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmExecutionConfig
import pl.edu.pw.ddm.platform.testing.interfaces.impl.DdmPipelineRunner
import spock.lang.Specification

class SvmWekaTestingSpec extends Specification {

    def "should perform correct classification of training data"() {
        given:
        def pipeline = new WekaClassifier()
        def miningMethod = new WekaClassifier()

        and:
        def config = DdmExecutionConfig.builder()
                .algorithmConfig(pipeline)
                .miningMethod(miningMethod)
                .dataPath([getClass().getResource('/data.train').path])
                .testDataPath(getClass().getResource('/data.train').path)
                .separator(',')
                .idIndex(0)
                .labelIndex(1)
                .attributesAmount(4)
                .colTypes(['numeric', 'nominal', 'numeric', 'numeric', 'numeric', 'numeric'] as String[])
                .distanceFunction(null)
                .executionParams(['options': '-C 12.5 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.50625"'])
                .build()
        def cr = new DdmPipelineRunner(config)

        when:
        def results = cr.run()

        then:
        results.results.size() == 8

        and:
        results.results[0].value == ' A'
        results.results[1].value == ' A'
        results.results[2].value == ' A'
        results.results[3].value == ' A'
        results.results[4].value == ' B'
        results.results[5].value == ' B'
        results.results[6].value == ' B'
        results.results[7].value == ' B'

        and: "print results"
        results.results
                .each { println it }
    }

}
