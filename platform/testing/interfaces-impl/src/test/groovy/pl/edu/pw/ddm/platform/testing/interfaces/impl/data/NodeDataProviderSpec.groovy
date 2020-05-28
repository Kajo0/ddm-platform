package pl.edu.pw.ddm.platform.testing.interfaces.impl.data

import spock.lang.Specification

class NodeDataProviderSpec extends Specification {

    def "should load data from path"() {
        given:
        def dataProvider = new NodeDataProvider(
                getClass().getResource('/iris.train').path,
                getClass().getResource('/iris.test').path,
                ',',
                0,
                5,
                4,
                ['numeric', 'numeric', 'numeric', 'numeric', 'numeric', 'nominal'] as String[]
        )

        when:
        def train = dataProvider.training()
        def test = dataProvider.test()
        def all = dataProvider.all()

        then:
        train.size() == 78
        test.size() == 72
        all.size() == 150

        and:
        train.first().id == '1'
        train.first().label == 'Iris-setosa'
        train.first().numericAttributes == [5.0, 3.0, 1.6, 0.2] as double[]

        and:
        test.first().id == '76'
        test.first().label == 'Iris-setosa'
        test.first().numericAttributes == [5.1, 3.5, 1.4, 0.2] as double[]
    }

}
