#!/usr/bin/python
import configparser
import json
import pprint
import requests
import sys
import time

lastExecFile = './last_exec.properties'
baseUrl = 'http://localhost:7000'
api = {
    'algorithm': {
        'broadcast': '/coordinator/command/algorithm/broadcast/instance/{instanceId}/{algorithmId}',
        'info': '/coordinator/command/algorithm/info',
        'load': '/coordinator/command/algorithm/load'
    },
    'data': {
        'distance-function-broadcast': '/coordinator/command/data/distance-function/broadcast/{instanceId}/{distanceFunctionId}',
        'distance-function-load': '/coordinator/command/data/distance-function/load/file',
        'info': '/coordinator/command/data/info',
        'distance-functions-info': '/coordinator/command/data/info/distance-functions',
        'partitioning-strategies-info': '/coordinator/command/data/info/partitioning-strategies',
        'load': '/coordinator/command/data/load/file',
        'loadUri': '/coordinator/command/data/load/uri',
        'partitioning-strategy-load': '/coordinator/command/data/partitioning-strategy/load/file',
        'scatter': '/coordinator/command/data/scatter/{instanceId}/{dataId}',
    },
    'execution': {
        'info': '/coordinator/command/execution/info',
        'collectLogs': '/coordinator/command/execution/logs/collect/{executionId}',
        'fetchLogs': '/coordinator/command/execution/logs/fetch/{executionId}/{nodeId}/{count}',
        'collectResults': '/coordinator/command/execution/results/collect/{executionId}',
        'start': '/coordinator/command/execution/start/{instanceId}/{algorithmId}/{trainDataId}',
        'status': '/coordinator/command/execution/status/{executionId}',
        'stop': '/coordinator/command/execution/stop/{executionId}'
    },
    'instance': {
        'config-update': '/coordinator/command/instance/config/{instanceId}/update',
        'info': '/coordinator/command/instance/info',
        'addresses': '/coordinator/command/instance/info/{instanceId}',
        'status': '/coordinator/command/instance/status/{instanceId}',

        'destroy': '/coordinator/command/instance/destroy/{instanceId}',
        'destroyAll': '/coordinator/command/instance/destroy/all',
        'create': '/coordinator/command/instance/create/{workers}'
    },
    'results': {
        'validate': '/coordinator/command/results/validate/{executionId}',
        'stats': '/coordinator/command/results/stats/{executionId}'
    }
}


def loadJar(path, debug=True):
    if debug:
        print("loadJar path='{}'".format(path))
    url = baseUrl + api['algorithm']['load']
    with open(path, 'rb') as file:
        algorithmId = requests.post(url,
                                    files={'file': (file.name, file, 'application/x-java-archive')}
                                    ).text
        if debug:
            print('  algorithmId: ' + algorithmId)
        return algorithmId


def algorithmInfo(debug=True):
    if debug:
        print('algorithmInfo')
    url = baseUrl + api['algorithm']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def broadcastJar(instanceId, algorithmId, debug=True):
    if debug:
        print("broadcastJar instanceId='{}' algorithmId='{}'".format(instanceId, algorithmId))
    url = baseUrl + api['algorithm']['broadcast'].format(**{
        'instanceId': instanceId,
        'algorithmId': algorithmId
    })
    response = requests.get(url).text
    if debug:
        print('  response: ' + response)
    return response


def dataInfo(debug=True):
    if debug:
        print('dataInfo')
    url = baseUrl + api['data']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def broadcastDistanceFunction(instanceId, distanceFunctionId, debug=True):
    if debug:
        print("broadcastDistanceFunction instanceId='{}' distanceFunctionId='{}'".format(instanceId, distanceFunctionId))
    url = baseUrl + api['data']['distance-function-broadcast'].format(**{
        'instanceId': instanceId,
        'distanceFunctionId': distanceFunctionId
    })
    response = requests.get(url).text
    if debug:
        print('  response: ' + response)
    return response


def loadDistanceFunction(path, debug=True):
    if debug:
        print("loadDistanceFunction path='{}'".format(path))
    url = baseUrl + api['data']['distance-function-load']
    with open(path, 'rb') as file:
        distanceFunctionId = requests.post(url, files={'distanceFunctionFile':
                                                           (file.name, file, 'application/x-java-archive')}).text
        if debug:
            print('  distanceFunctionId: ' + distanceFunctionId)
        return distanceFunctionId


def loadPartitioningStrategy(path, debug=True):
    if debug:
        print("loadPartitioningStrategy path='{}'".format(path))
    url = baseUrl + api['data']['partitioning-strategy-load']
    with open(path, 'rb') as file:
        partitioningStrategyId = requests.post(url, files={'partitioningStrategyFile':
                                                               (file.name, file, 'application/x-java-archive')}).text
        if debug:
            print('  partitioningStrategyId: ' + partitioningStrategyId)
        return partitioningStrategyId


def functionsInfo(debug=True):
    if debug:
        print('functionsInfo')
    url = baseUrl + api['data']['distance-functions-info']
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def strategiesInfo(debug=True):
    if debug:
        print('strategiesInfo')
    url = baseUrl + api['data']['partitioning-strategies-info']
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def loadData(path, labelIndex, separator=',', idIndex=None, vectorizeStrings=False, percentage=None, debug=True):
    if debug:
        print(
            "loadData path='{}' idIndex='{}' labelIndex='{}' separator='{}' vectorizeStrings='{}' percentage='{}'".format(
                path,
                idIndex,
                labelIndex,
                separator,
                vectorizeStrings,
                percentage))
    url = baseUrl + api['data']['load']
    with open(path, 'rb') as file:
        dataId = requests.post(url,
                               files={'dataFile': (file.name, file, 'application/x-java-archive')},
                               data={
                                   'idIndex': idIndex,
                                   'labelIndex': labelIndex,
                                   'separator': separator,
                                   'deductType': not vectorizeStrings,
                                   'vectorizeStrings': vectorizeStrings,
                                   'extractTrainPercentage': percentage
                               }
                               ).text
        if debug:
            print('  dataId: ' + dataId)
        return dataId


def scatterData(instanceId, dataId, strategy='uniform', strategyParams=None, distanceFunction=None, typeCode='train',
                seed=None, debug=True):
    if debug:
        print(
            "scatterData instanceId='{}' dataId='{}' strategy='{}' strategyParams='{}' distanceFunction='{}' typeCode='{}' seed='{}'".format(
                instanceId,
                dataId,
                strategy,
                strategyParams,
                distanceFunction,
                typeCode,
                seed))
    url = baseUrl + api['data']['scatter'].format(**{
        'instanceId': instanceId,
        'dataId': dataId
    })
    response = requests.post(url,
                             data={
                                 'strategy': strategy,
                                 'strategyParams': strategyParams,
                                 'distanceFunction': distanceFunction,
                                 'typeCode': typeCode,
                                 'seed': seed
                             }
                             ).text
    if debug:
        print('  response: ' + response)
    return response


def executionInfo(debug=True):
    if debug:
        print('executionInfo')
    url = baseUrl + api['execution']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def collectLogs(executionId, debug=True):
    if debug:
        print("collectLogs executionId='{}'".format(executionId))
    url = baseUrl + api['execution']['collectLogs'].format(**{'executionId': executionId})
    response = requests.get(url).text
    if debug:
        print('  response: ' + response)
    return response


def fetchLogs(executionId, nodeId, count, debug=True):
    if debug:
        print("fetchLogs executionId='{}' nodeId='{}' count='{}'".format(executionId, nodeId, count))
    url = baseUrl + api['execution']['fetchLogs'].format(**{
        'executionId': executionId,
        'nodeId': nodeId,
        'count': count
    })
    response = requests.get(url).text
    if debug:
        print('  response size: ' + str(len(response)))
    return response


def collectResults(executionId, debug=True):
    if debug:
        print("collectResults executionId='{}'".format(executionId))

    # TODO check if results are collected instead of always collecting
    status = executionStatus(executionId, False)
    try:
        if status['status'] != 'FINISHED':
            raise ValueError(status['status'])
    except ValueError as err:
        print('Not finished or started: ', err)
        return err

    url = baseUrl + api['execution']['collectResults'].format(**{'executionId': executionId})
    response = requests.get(url).text
    if debug:
        print('  response: ' + response)
    return response


def startExecution(instanceId, algorithmId, trainDataId, testDataId=None, distanceFuncName='none', params=None, debug=True):
    if debug:
        print(
            "startExecution instanceId='{}' algorithmId='{}' trainDataId='{}' testDataId='{}' distanceFuncName='{}' params='{}'".format(
                instanceId,
                algorithmId,
                trainDataId,
                testDataId,
                distanceFuncName,
                params))
    url = baseUrl + api['execution']['start'].format(**{
        'instanceId': instanceId,
        'algorithmId': algorithmId,
        'trainDataId': trainDataId
    })

    if not params:
        params = {
            'seed': str(int(round(time.time()))),
            'groups': '3',
            'iterations': '20',
            'epsilon': '0.002',
            'distanceFunctionName': distanceFuncName,
            # 'distanceFunctionId': '1156746230', # loaded equality
            'preCalcCentroids': 'true',
            'b': '2',
            'meb_clusters': '50',
            'kernel': 'linear', #rbf #linear
            'knn_k': '3',
            'use_local_classifier': 'false'
        }
    jsonParams = json.dumps(params)

    executionId = requests.post(url,
                                data={
                                    'testDataId': testDataId,
                                    'executionParams': jsonParams
                                }
                                ).text
    if debug:
        print('  executionId: ' + executionId)
    return executionId


def executionStatus(executionId, debug=True):
    if debug:
        print("executionStatus executionId='{}'".format(executionId))
    url = baseUrl + api['execution']['status'].format(**{'executionId': executionId})
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def createInstance(workers, cpu=2, memory=2, disk=10, debug=True):
    if debug:
        print("createInstance workers='{}' cpu='{}' memory='{}' disk='{}'".format(workers, cpu, memory, disk))
    url = baseUrl + api['instance']['create'].format(**{'workers': workers})
    instanceId = requests.post(url,
                               data={
                                   'cpu': cpu,
                                   'memory': memory,
                                   'disk': disk
                               }
                               ).text
    if debug:
        print('  instanceId: ' + instanceId)
    return instanceId


def instanceStatus(instanceId, debug=True):
    if debug:
        print("instanceStatus instanceId='{}'".format(instanceId))
    url = baseUrl + api['instance']['status'].format(**{'instanceId': instanceId})
    response = requests.get(url)
    if debug:
        print('  response: ' + response.text)
    return response.status_code


def instanceConfigUpdate(instanceId, debug=True):
    if debug:
        print("instanceConfigUpdate instanceId='{}'".format(instanceId))
    url = baseUrl + api['instance']['config-update'].format(**{'instanceId': instanceId})
    response = requests.get(url)
    if debug:
        print('  response: ' + response.text)
    return response.status_code


def instanceInfo(debug=True):
    if debug:
        print('instanceInfo')
    url = baseUrl + api['instance']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def destroyAll(debug=True):
    if debug:
        print('destroyAll')
    url = baseUrl + api['instance']['destroyAll']
    response = requests.get(url).text
    if debug:
        print('  response: ' + response)
    return response


def validateResults(executionId, metrics, debug=True):
    if debug:
        print("validateResults executionId='{}' metrics='{}'".format(executionId, metrics))

    if isinstance(collectResults(executionId, False), ValueError):
        return

    url = baseUrl + api['results']['validate'].format(**{'executionId': executionId})
    response = requests.post(url,
                             data={'metrics': metrics}
                             ).text
    if debug:
        print('  response: ' + response)
    return json.loads(response)


def resultsStats(executionId, debug=True):
    if debug:
        print("resultsStats executionId='{}' ".format(executionId))

    if isinstance(collectResults(executionId, False), ValueError):
        return

    url = baseUrl + api['results']['stats'].format(**{'executionId': executionId})
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def saveLast(oneNode, instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId=None,
             partitioningStrategyId=None, executionId=None):
    config = configparser.RawConfigParser()
    config['onenode' if oneNode else 'last'] = {
        'instance_id': instanceId,
        'algorithm_id': algorithmId,
        'train_data_id': trainDataId,
        'test_data_id': testDataId,
        'distance_function_id': distanceFunctionId,
        'partitioning_strategy_id': partitioningStrategyId,
        'execution_id': executionId
    }
    try:
        config['onenode' if not oneNode else 'last'] = loadLast(not oneNode)
    except:
        print('No previous onenode=', oneNode, 'save but its ok')

    with open(lastExecFile, 'w') as file:
        config.write(file)


def loadLast(oneNode):
    config = configparser.RawConfigParser()
    config.read(lastExecFile)
    return dict(config['onenode' if oneNode else 'last'])


def setupDefault(workers=2, cpu=2, memory=2, oneNode=False):
    algorithmId = None
    if oneNode:
        algorithmId = loadJar('./samples/k-means-weka.jar')
    else:
        algorithmId = loadJar('./samples/aoptkm.jar')

    trainDataId = loadData('./samples/iris.data', 4, ',', None)
    testDataId = loadData('./samples/iris.test', 4, ',', None)
    distanceFunctionId = loadDistanceFunction('./samples/equality-distance.jar')
    partitioningStrategyId = loadPartitioningStrategy('./samples/dense-and-outliers-strategy.jar')
    instanceId = createInstance(workers, cpu, memory, 10)  # cpu, memory, disk

    print('Wait for setup', end='', flush=True)
    while instanceStatus(instanceId, False) != 200:
        print('.', end='', flush=True)
        time.sleep(2)
    print('')

    broadcastJar(instanceId, algorithmId)
    scatterData(instanceId, trainDataId, 'uniform', None, None, 'train')
    scatterData(instanceId, testDataId, 'dummy', None, None, 'test')
    broadcastDistanceFunction(instanceId, distanceFunctionId)
    instanceConfigUpdate(instanceId)

    saveLast(oneNode, instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId, partitioningStrategyId)


def reload(oneNode=False):
    last = loadLast(oneNode)
    instanceId = last.get('instance_id')

    algorithmId = None
    if oneNode:
        algorithmId = loadJar('./samples/k-means-weka.jar')
        # algorithmId = loadJar('./samples/svm-weka.jar')
    else:
        algorithmId = loadJar('./samples/aoptkm.jar')
        # algorithmId = loadJar('./samples/dkmeans.jar')
        # algorithmId = loadJar('./samples/lct.jar')
        # algorithmId = loadJar('./samples/dmeb.jar')
        # algorithmId = loadJar('./samples/dmeb-2.jar')
        # algorithmId = loadJar('./samples/random-classifier.jar')

    vectorizeStrings = False
    # vectorizeStrings = True
    trainPercentage = None
    # trainPercentage = 10
    if not trainPercentage:
        # trainDataId = loadData('./samples/iris.data', 4, ',', None, vectorizeStrings, trainPercentage)
        trainDataId = loadData('./samples/iris_numeric.data', 4, ',', None, vectorizeStrings, trainPercentage)
        # testDataId = loadData('./samples/iris.test', 4, ',', None, vectorizeStrings, trainPercentage)
        testDataId = loadData('./samples/iris_numeric.test', 4, ',', None, vectorizeStrings, trainPercentage)
    else:
        ids = loadData('./samples/iris.data', 4, ',', None, vectorizeStrings, trainPercentage)
        trainDataId, testDataId = ids.split(',')

    distanceFunctionId = loadDistanceFunction('./samples/equality-distance.jar')
    partitioningStrategyId = loadPartitioningStrategy('./samples/dense-and-outliers-strategy.jar')

    broadcastJar(instanceId, algorithmId)
    seed = None
    # seed = 8008135
    if oneNode:
        scatterData(instanceId, trainDataId, 'uniform', None, None, 'train', seed)
    else:
        scatterData(instanceId, trainDataId, 'uniform', None, None, 'train', seed)
        # scatterData(instanceId, trainDataId, 'separate-labels', 'Iris-setosa|Iris-virginica,Iris-versicolor', None, 'train', seed)
        # scatterData(instanceId, trainDataId, 'dense-and-outliers', '0.6', 'euclidean', 'train', seed)
        # scatterData(instanceId, trainDataId, 'most-of-one-plus-some', 'fillEmptyButPercent=0.8;additionalClassesNumber=2;additionalClassesPercent=0.05', None, 'train', seed)

    scatterData(instanceId, testDataId, 'dummy', None, None, 'test')
    broadcastDistanceFunction(instanceId, distanceFunctionId)
    instanceConfigUpdate(instanceId)

    saveLast(oneNode, instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId, partitioningStrategyId)


def instStatus(oneNode=False):
    last = loadLast(oneNode)
    instanceId = last.get('instance_id')
    print(instanceId)
    instanceStatus(instanceId)


def instConfUpdate(oneNode=False):
    last = loadLast(oneNode)
    instanceId = last.get('instance_id')
    instanceConfigUpdate(instanceId)


def execute(oneNode=False):
    last = loadLast(oneNode)
    instanceId = last.get('instance_id')
    algorithmId = last.get('algorithm_id')
    trainDataId = last.get('train_data_id')
    testDataId = last.get('test_data_id')
    distanceFunctionId = last.get('distance_function_id')
    partitioningStrategyId = last.get('partitioning_strategy_id')

    executionId = startExecution(instanceId, algorithmId, trainDataId, testDataId)
    # executionId = startExecution(instanceId, algorithmId, trainDataId, testDataId, 'euclidean')
    # executionId = startExecution(instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId)

    saveLast(oneNode, instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId, partitioningStrategyId,
             executionId)


def status(oneNode=False):
    last = loadLast(oneNode)
    executionStatus(last.get('execution_id'))


def logs(oneNode=False):
    last = loadLast(oneNode)
    collectLogs(last.get('execution_id'))


def lastlog(oneNode=False):
    last = loadLast(oneNode)
    executionId = last.get('execution_id')
    appId = executionStatus(executionId, False)['appId']
    nodes = instanceInfo(False)[last.get('instance_id')]['nodes']

    collectLogs(executionId)

    logs = {}
    for nodeId in nodes:
        n = nodes[nodeId]
        logs[nodeId] = {}
        logs[nodeId]['type'] = n['type']
        logs[nodeId]['log'] = fetchLogs(executionId, nodeId, -10)

    for log in logs:
        print('\n\n     =====================================================> ' + logs[log]['type'] + ' [' + log + ']')
        print(logs[log]['log'])


def results(oneNode=False):
    last = loadLast(oneNode)
    collectResults(last.get('execution_id'))


def stats(oneNode=False):
    last = loadLast(oneNode)
    resultsStats(last.get('execution_id'))


def validate(oneNode=False):
    last = loadLast(oneNode)
    validateResults(last.get('execution_id'), 'accuracy,recall,precision,f-measure,ARI')


def clear():
    destroyAll()


def checkExist(resourceType, id):
    try:
        if resourceType == 'data':
            return dataInfo(False)[id]
        elif resourceType == 'alg':
            return algorithmInfo(False)[id]
        else:
            raise ValueError('Unknown resource type ' + resourceType)
    except KeyError:
        raise ValueError('Not found ' + resourceType + ' with id: ' + id)


def schedule():
    #   '1334849234' '1333828439' = FULL       -> iris_numeric.data, iris_numeric.test
    #   '1187478398' '238800836'  = 30% train  -> Skin_NonSkin.txt
    #   '519015334'  '405676659'  = FULL       -> poker-hand-training-true.data, poker-hand-testing.data
    #   '2026464871' '1043020749' = FULL       -> creditcard.csv
    #   '1959356483' '1791383717' = FULL       -> shuttle.trn, shuttle.tst
    #   '1338339913' '1401394455' = FULL       -> adult.data, adult.test
    #       trainId   testId
    irisNumeric = (loadData('./samples/iris_numeric.data', 4, ',', None, False, None, False),
                   loadData('./samples/iris_numeric.test', 4, ',', None, False, None, False))
    # SkinNonSkin = tuple(loadData('/home/kajo/Downloads/2020-12-03-svm-data/Skin_NonSkin.txt', 3, '	', None, False, 30, False).split(','))
    # adult = (loadData('/home/kajo/Downloads/2020-12-03-svm-data/adult.data', 14, ',', None, True, None, False),
    #          loadData('/home/kajo/Downloads/2020-12-03-svm-data/adult.test', 14, ',', None, True, None, False))
    data = [irisNumeric]
    # workers cpus memory
    instances = [
        (1, 2, 8),
        (2, 2, 4),
        (4, 2, 3),
        (8, 2, 2)
    ]
    #     strategy seed custom-params multiNode
    strategies = [
        ('uniform', 11, None, False),
        ('most-of-one-plus-some', 11, 'fillEmptyButPercent=0.99;additionalClassesNumber=0;additionalClassesPercent=0', True),
        ('most-of-one-plus-some', 11, 'fillEmptyButPercent=0.8;additionalClassesNumber=2;additionalClassesPercent=0.05', True)
    ]
    #   '1859600396' = 'WEKA SVM',
    #   '539897355'  = 'D-MEB'
    #   '1826773956' = 'D-MEB-2'
    wekaSvm = loadJar('./samples/svm-weka.jar', False)
    dmeb = loadJar('./samples/dmeb.jar', False)
    dmeb2 = loadJar('./samples/dmeb-2.jar', False)
    # algorithmId    params distanceFunctionName distanceFunctionId multiNode
    executions = [
        (wekaSvm, {'kernel': 'linear'}, 'euclidean', None, False),
        (wekaSvm, {'kernel': 'rbf'}, 'euclidean', None, False),
        (dmeb, {'kernel': 'linear', 'meb_clusters': '50'}, 'euclidean', None, True),
        (dmeb, {'kernel': 'linear', 'meb_clusters': '-1'}, 'euclidean', None, True),
        (dmeb, {'kernel': 'rbf', 'meb_clusters': '50'}, 'euclidean', None, True),
        (dmeb, {'kernel': 'rbf', 'meb_clusters': '-1'}, 'euclidean', None, True),
        (dmeb2, {'kernel': 'linear', 'meb_clusters': '50', 'knn_k': '3', 'use_local_classifier': 'false'}, 'euclidean', None, True),
        (dmeb2, {'kernel': 'linear', 'meb_clusters': '-1', 'knn_k': '3', 'use_local_classifier': 'false'}, 'euclidean', None, True),
        (dmeb2, {'kernel': 'rbf', 'meb_clusters': '50', 'knn_k': '3', 'use_local_classifier': 'false'}, 'euclidean', None, True),
        (dmeb2, {'kernel': 'rbf', 'meb_clusters': '-1', 'knn_k': '3', 'use_local_classifier': 'false'}, 'euclidean', None, True),
        (dmeb2, {'kernel': 'linear', 'meb_clusters': '50', 'knn_k': '3', 'use_local_classifier': 'true'}, 'euclidean', None, True),
        (dmeb2, {'kernel': 'linear', 'meb_clusters': '-1', 'knn_k': '3', 'use_local_classifier': 'true'}, 'euclidean', None, True),
        (dmeb2, {'kernel': 'rbf', 'meb_clusters': '50', 'knn_k': '3', 'use_local_classifier': 'true'}, 'euclidean', None, True),
        (dmeb2, {'kernel': 'rbf', 'meb_clusters': '-1', 'knn_k': '3', 'use_local_classifier': 'true'}, 'euclidean', None, True)
    ]

    # check data
    for d in data:
        checkExist('data', d[0])
        checkExist('data', d[1])
    # check algorithms
    for execution in executions:
        checkExist('alg', execution[0])
    # strategy may be included - not loaded

    debug = False

    for d in data:
        print(' data:', d)
        trainDataId = d[0]
        testDataId = d[1]

        for instance in instances:
            oneNode = instance[0] == 1
            print('  instance:', instance, 'onenode:', oneNode)
            workers = instance[0]
            cpu = instance[1]
            memory = instance[2]
            disk = 10 # not really used for dockers

            print('  Clearing previous', end='', flush=True)
            destroyAll(debug)
            instanceId = createInstance(workers, cpu, memory, disk, debug)

            print(' and wait for setup', end='', flush=True)
            while instanceStatus(instanceId, False) != 200:
                print('.', end='', flush=True)
                time.sleep(2)
            print('')
            instanceConfigUpdate(instanceId, debug)

            for strategy in strategies:
                print('   strategy:', strategy)
                strategyName = strategy[0]
                strategySeed = strategy[1]
                strategyParams = strategy[2]
                multiNode = strategy[3]

                if oneNode and multiNode:
                    print('   partitioning strategy requires multiple nodes, so ommit')
                    continue

                scatterData(instanceId, trainDataId, strategyName, strategyParams, None, 'train', strategySeed, debug)
                if testDataId:
                    scatterData(instanceId, testDataId, 'dummy', None, None, 'test', strategySeed, debug)

                for execution in executions:
                    print('    execution:', execution)
                    algorithmId = execution[0]
                    executionParams = execution[1]
                    distanceFunctionName = execution[2]
                    distanceFunctionId = execution[3]
                    multiNode = execution[4]

                    if oneNode and multiNode:
                        print('    distributed algorithm requires multiple nodes, so ommit')
                        continue
                    elif not oneNode and not multiNode:
                        print('    local algorithm requires one node, so ommit')
                        continue

                    broadcastJar(instanceId, algorithmId, debug)
                    executionId = startExecution(instanceId, algorithmId, trainDataId, testDataId, distanceFunctionName, executionParams, debug)

                    print('    Wait for finish of: ' + executionId + ' ', end='', flush=True)
                    status = 'UNKNOWN'
                    while status != 'FINISHED' and status != 'FAILED' and status != 'STOPPED':
                        time.sleep(30)
                        try:
                            status = executionStatus(executionId, False)['status']
                        except KeyError:
                            status = 'UNKNOWN'
                        print('.', end='', flush=True)
                    print('')

                    metrics = validateResults(executionId, 'accuracy,recall,precision,f-measure,ARI', debug)
                    print('     METRICS:', metrics)
                    stats = resultsStats(executionId, debug)

                    headers = []
                    headers.append('data')
                    headers.append('nodes')
                    headers.append('strategy')
                    headers.append('strategyParams')
                    headers.append('algorithm')
                    headers.append('algorithmParams')
                    headers.append('kernel')
                    headers.append('mebClusters')
                    headers.append('use-local')
                    headers.append('ARI')
                    headers.append('f-measure')
                    headers.append('accuracy')
                    headers.append('recall')
                    headers.append('precision')
                    headers.append('ddmTotalProcessing')
                    headers.append('localBytes')
                    headers.append('globalBytes')
                    headers.append('trainingBytes')
                    print('         CSV_READY_HEADER', ';'.join(headers))
                    values = []
                    values.append(checkExist('data', trainDataId)['originalName'])
                    values.append(workers)
                    values.append(strategyName)
                    values.append(str(strategyParams).replace(';', '|'))
                    values.append(checkExist('alg', algorithmId)['algorithmName'])
                    values.append(str(executionParams).replace(';', '|'))
                    values.append(executionParams.get('kernel', ''))
                    values.append(executionParams.get('meb_clusters', ''))
                    values.append(executionParams.get('use_local_classifier', ''))
                    values.append(metrics.get('ARI', ''))
                    values.append(metrics.get('f-measure', ''))
                    values.append(metrics.get('accuracy', ''))
                    values.append(metrics.get('recall', ''))
                    values.append(metrics.get('precision', ''))
                    values.append(stats['time']['ddmTotalProcessing'])
                    values.append(stats['transfer']['localBytes'])
                    values.append(stats['transfer']['globalBytes'])
                    values.append(stats['data']['trainingBytes'])
                    values = list(map(str, values))
                    print('         CSV_READY_VALUES', ';'.join(values))
    print('  Clearing instances')
    destroyAll(debug)

    print('')
    print('SCHEDULE FINISHED')


if len(sys.argv) < 2:
    print(
        '  Provide command! [setup, inststatus, confupdate, clear, reload, execute, status, logs, lastlog, results, validate, stats, info [data, alg, func, strgy, exec, inst]]')
    sys.exit(1)

command = sys.argv[1]
oneNode = False
if len(sys.argv) > 2 and sys.argv[2] == 'onenode':
    oneNode = True

if command == 'setup':
    if oneNode:
        setupDefault(1, 2, 4, True)
    else:
        setupDefault(2, 2, 2, False)
elif command == 'inststatus':
    instStatus(oneNode)
elif command == 'confupdate':
    instConfUpdate(oneNode)
elif command == 'clear':
    clear()
elif command == 'reload':
    reload(oneNode)
elif command == 'execute':
    execute(oneNode)
elif command == 'status':
    status(oneNode)
elif command == 'logs':
    logs(oneNode)
elif command == 'lastlog':
    lastlog(oneNode)
elif command == 'results':
    results(oneNode)
elif command == 'stats':
    stats(oneNode)
elif command == 'validate':
    validate(oneNode)
elif command == 'schedule':
    schedule()
elif command == 'info':
    if len(sys.argv) < 3:
        print('  Provide info arg [data, func, strgy, alg, exec, inst]')
        sys.exit(1)

    arg = sys.argv[2]
    if arg == 'data':
        dataInfo()
    elif arg == 'func':
        functionsInfo()
    elif arg == 'strgy':
        strategiesInfo()
    elif arg == 'alg':
        algorithmInfo()
    elif arg == 'exec':
        executionInfo()
    elif arg == 'inst':
        instanceInfo()
    else:
        print('  Unknown info to show')
else:
    print('  Unknown command')
