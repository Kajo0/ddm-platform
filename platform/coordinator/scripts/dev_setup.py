#!/usr/bin/python
import configparser
import json
import pprint
import re
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
        'partitioning': '/coordinator/command/data/partitioning/{dataId}',
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
        print(
            "broadcastDistanceFunction instanceId='{}' distanceFunctionId='{}'".format(instanceId, distanceFunctionId))
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


def loadData(path, labelIndex, separator=',', idIndex=None, vectorizeStrings=False, percentage=None, seed=None,
             debug=True, expandAmount=None):
    if debug:
        print(
            "loadData path='{}' idIndex='{}' labelIndex='{}' separator='{}' vectorizeStrings='{}' percentage='{}' seed='{}' expandAmount='{}'".format(
                path,
                idIndex,
                labelIndex,
                separator,
                vectorizeStrings,
                percentage,
                seed,
                expandAmount))
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
                                   'extractTrainPercentage': percentage,
                                   'expandAmount': expandAmount,
                                   'seed': seed
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


def partitionData(dataId, partitions, strategy='uniform', strategyParams=None, distanceFunction=None, seed=None,
                  debug=True):
    if debug:
        print(
            "partitionData dataId='{}' partitions='{}' strategy='{}' strategyParams='{}' distanceFunction='{}' seed='{}'".format(
                dataId, partitions, strategy, strategyParams, distanceFunction, seed))
    url = baseUrl + api['data']['partitioning'].format(**{
        'dataId': dataId
    })
    response = requests.post(url,
                             data={
                                 'partitions': partitions,
                                 'strategy': strategy,
                                 'strategyParams': strategyParams,
                                 'distanceFunction': None if distanceFunction == 'None' else distanceFunction,
                                 'seed': None if seed == 'None' else seed
                             }
                             ).text
    if debug:
        print('  response: ' + response)
    return response


def partition(partitions, strategy, strategyParams, distanceFunction, seed, oneNode=False, debug=True):
    if debug:
        print('partition')
    last = loadLast(oneNode)
    trainDataId = last.get('train_data_id')

    partitionData(trainDataId, partitions, strategy, strategyParams, distanceFunction, seed, debug)


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


def startExecution(instanceId, algorithmId, trainDataId, testDataId=None, distanceFuncName='none', params=None,
                   debug=True):
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
        # FIXME setup default params
        params = {
            'seed': str(int(round(time.time()))),
            'groups': '6',
            'iterations': '20',
            'epsilon': '0.002',
            'distanceFunctionName': distanceFuncName,
            # 'distanceFunctionId': '1156746230', # loaded equality
            'preCalcCentroids': 'true',
            'b': '2',
            'meb_clusters': '-1',
            'kernel': 'rbf',  # rbf #linear # svm's
            'knn_k': '3',
            'init_kmeans_method': 'k-means++',  # 'Random'
            'use_local_classifier': 'false',  # 2lvl-svm
            'branching_factor': '50',  # dbirch
            'threshold': '0.01',  # dbirch
            'g_groups': '7',  # dbirch
            'g_threshold': '0.01',  # dbirch
            'local_method_name': 'only_with_svs',  # 2lvl-svm
            'first_level_classification_result': 'true',  # 2lvl-svm
            # 'noOneGroup': 'true', # aoptkm
            # 'minKGroups': 'true', # aoptkm
            # 'exactKGroups': 'true' # aoptkm
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


def createInstance(workers, cpu=2, workerMemory=2, masterMemory=2, disk=10, debug=True):
    if debug:
        print("createInstance workers='{}' cpu='{}' workerMemory='{}' masterMemory='{}' disk='{}'".format(workers, cpu,
                                                                                                          workerMemory,
                                                                                                          masterMemory,
                                                                                                          disk))
    url = baseUrl + api['instance']['create'].format(**{'workers': workers})
    instanceId = requests.post(url,
                               data={
                                   'cpu': cpu,
                                   'workerMemory': workerMemory,
                                   'masterMemory': masterMemory,
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
        print("resultsStats executionId='{}'".format(executionId))

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


def setupDefault(workers=2, cpu=2, workerMemory=2, masterMemory=2, oneNode=False):
    # FIXME setup some algorithm to check that loading works
    algorithmId = None
    if oneNode:
        algorithmId = loadJar('./samples/k-means-weka.jar')
    else:
        algorithmId = loadJar('./samples/dkmeans.jar')

    # FIXME setup some data to check that loading works
    trainDataId = loadData('./samples/iris.data', 4, ',', None)
    testDataId = loadData('./samples/iris.test', 4, ',', None)
    # FIXME setup some distance function to check that loading works
    distanceFunctionId = loadDistanceFunction('./samples/equality-distance.jar')
    # FIXME setup some partitioning strategy to check that loading works
    partitioningStrategyId = loadPartitioningStrategy('./samples/dense-and-outliers-strategy.jar')

    instanceId = createInstance(workers, cpu, workerMemory, masterMemory, 10)  # cpu, workerMemory, masterMemory disk

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

    # FIXME setup default reload algorithm
    algorithmId = None
    if oneNode:
        algorithmId = loadJar('./samples/k-means-weka.jar')
        # algorithmId = loadJar('./samples/svm-weka.jar')
    else:
        algorithmId = loadJar('./samples/dkmeans.jar')
        # algorithmId = loadJar('./samples/aoptkm.jar')
        # algorithmId = loadJar('./samples/lct.jar')
        # algorithmId = loadJar('./samples/dbirch.jar')
        # algorithmId = loadJar('./samples/dmeb.jar')
        # algorithmId = loadJar('./samples/dmeb-2.jar')
        # algorithmId = loadJar('./samples/svm-2lvl.jar')
        # algorithmId = loadJar('./samples/random-classifier.jar')
        # algorithmId = loadJar('./samples/naive-bayes.jar')

    # FIXME setup default reload string vectorization
    vectorizeStrings = False
    # vectorizeStrings = True

    # FIXME setup default reload training percentage
    trainPercentage = None
    # trainPercentage = 10
    if not trainPercentage:
        # FIXME setup default reload training sample numerical data
        # trainDataId = loadData('./samples/iris.data', 4, ',', None, vectorizeStrings, trainPercentage)
        trainDataId = loadData('./samples/iris_numeric.data', 4, ',', None, vectorizeStrings, trainPercentage)
        # testDataId = loadData('./samples/iris.test', 4, ',', None, vectorizeStrings, trainPercentage)
        testDataId = loadData('./samples/iris_numeric.test', 4, ',', None, vectorizeStrings, trainPercentage)
    else:
        # FIXME setup default reload training sample nominal data
        ids = loadData('./samples/iris.data', 4, ',', None, vectorizeStrings, trainPercentage)
        trainDataId, testDataId = ids.split(',')

    # FIXME setup non-default one-time data for reload
    trainDataId = loadData(
        './samples/iris_numeric.data',  # path
        4,  # label index
        ',',  # separator
        None,  # identifier index
        False,  # vectorization flag
        None,  # train percentage
        2022,  # seed
        False,  # debug flag
        None  # expand amount
    )
    testDataId = loadData(
        './samples/iris_numeric.test',  # path
        4,  # label index
        ',',  # separator
        None,  # identifier index
        False,  # vectorization flag
        None,  # train percentage
        2022,  # seed
        False,  # debug flag
        None  # expand amount
    )

    # FIXME setup non-default one-time distance function for reload
    distanceFunctionId = loadDistanceFunction('./samples/equality-distance.jar')
    # FIXME setup non-default one-time partitioning strategy for reload
    partitioningStrategyId = loadPartitioningStrategy('./samples/dense-and-outliers-strategy.jar')

    broadcastJar(instanceId, algorithmId)

    # FIXME setup non-default seed for partitioning scatter when non-deterministic
    seed = None
    # seed = 8008135

    if oneNode:
        scatterData(instanceId, trainDataId, 'uniform', None, None, 'train', seed)
    else:
        # FIXME setup non-default partitioning data scattering
        scatterData(instanceId, trainDataId, 'uniform', '', None, 'train', seed)  # 'iid-u'
        # scatterData(instanceId, trainDataId, 'dense-and-outliers', '0.7', 'euclidean', 'train', seed)  # 'cs-dao'
        # scatterData(instanceId, trainDataId, 'covariate-shift', 'attribute=0;shift=0.3;splits=3;method=0', None, 'train', seed)  # 'cs-dsad'
        # scatterData(instanceId, trainDataId, 'most-of-one-plus-some', 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', None, 'train', seed)  # 'pps-s'
        # scatterData(instanceId, trainDataId, 'most-of-one-plus-some', 'additionalClassesNumber=-1;emptyWorkerFill=9;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', None, 'train', seed)  # 'pps-ab'
        # scatterData(instanceId, trainDataId, 'most-of-one-plus-some', 'additionalClassesNumber=10;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', None, 'train', seed)  # 'pps-mpa'
        # scatterData(instanceId, trainDataId, 'most-of-one-plus-some', 'additionalClassesNumber=-3;emptyWorkerFill=7;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', None, 'train', seed)  # 'pps-ms'
        # scatterData(instanceId, trainDataId, 'most-of-one-plus-some', 'additionalClassesNumber=2;emptyWorkerFill=8;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', None, 'train', seed)  # 'pps-mps'
        # scatterData(instanceId, trainDataId, 'concept-drift', 'drifts=3;discreteRanges=40;label=1', None, 'train', seed)  # 'cd-sba'
        # scatterData(instanceId, trainDataId, 'concept-shift', 'shifts=2;label=1', None, 'train', seed)  # 'cs-cl'
        # scatterData(instanceId, trainDataId, 'unbalancedness', 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', None, 'train', seed)  # 'u-ul'

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

    # FIXME setup non-default one-time execution distance function by name or identifier
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
        logs[nodeId]['log'] = fetchLogs(executionId, nodeId, -100)  # FIXME setup by default fetch all logs, negative value means last 'n' lines

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
    validateResults(last.get('execution_id'),
                    'accuracy,recall,precision,f-measure,ARI')  # AMI - long time calculation when more data  # FIXME setup default all measures


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


def checkIsResponseError(response):
    # TODO make it more sophisticated
    if response == 'ok_process-id':
        return False
    formatted = json.loads(response)
    return formatted['status'] == 500


def schedule():
    dataSeed = 2022
    strategySeed = 2022
    executionSeed = None

    # FIXME setup schedule data
    # train data id, test data id, groups number
    irisNumeric = (loadData('./samples/iris_numeric.data', 4, ',', None, False, None, dataSeed, False),
                   loadData('./samples/iris_numeric.test', 4, ',', None, False, None, dataSeed, False),
                   3)

    data = [irisNumeric]

    # FIXME setup schedule instances
    # workers cpus workerMemory masterMemory
    instances = [
        # (1, 2, 8, 8),
        (3, 2, 4, 4),
        # (6, 2, 3, 4),
        # (12, 1, 2, 4)
    ]

    # FIXME setup schedule partitioning strategies to avoid in multi node execution
    avoidMultiNodeStrategiesAlias = []
    # FIXME setup schedule load external partitioning strategy
    denseOutliersStrategyId = loadPartitioningStrategy('./samples/dense-and-outliers-strategy.jar', False)

    #     printAlias strategy seed custom-params multiNode distanceFunction
    strategies = [
        # FIXME setup schedule uniform as default
        # ('iid-u', 'uniform', strategySeed, '', False, None),

        # ### for dataset: iris_numeric.data
        ('iid-u', 'uniform', strategySeed, '', True, None),
        # ('cs-dao', 'dense-and-outliers', strategySeed, '0.7', True, 'euclidean'),
        # ('cs-dsad', 'covariate-shift', strategySeed, 'attribute=0;shift=0.3;splits=3;method=0', True, None),
        # ('pps-s', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', True, None),
        # ('pps-ab', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=2;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', True, None),
        # ('pps-mpa', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=3;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', True, None),
        # ('pps-ms', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-2;emptyWorkerFill=1;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', True, None),
        # ('pps-mps', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=2;emptyWorkerFill=1;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', True, None),
        # ('cd-sba', 'concept-drift', strategySeed, 'drifts=3;discreteRanges=40;label=77', True, None),
        # ('cs-cl', 'concept-shift', strategySeed, 'shifts=2;label=77', True, None),
        # ('u-ul', 'unbalancedness', strategySeed, 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', True, None),

        # ### for dataset: 28_mnist_train.csv
        # ('iid-u', 'uniform', strategySeed, '', True, None),
        # ('cs-dao', 'dense-and-outliers', strategySeed, '0.7', True, 'euclidean'),
        # ('cs-dsad', 'covariate-shift', strategySeed, 'attribute=0;shift=0.3;splits=3;method=0', True, None),
        # ('pps-s', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', True, None),
        # ('pps-ab', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=9;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', True, None),
        # ('pps-mpa', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=10;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', True, None),
        # ('pps-ms', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-3;emptyWorkerFill=7;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', True, None),
        # ('pps-mps', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=2;emptyWorkerFill=8;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', True, None),
        # ('cd-sba', 'concept-drift', strategySeed, 'drifts=3;discreteRanges=40;label=1', True, None),
        # ('cs-cl', 'concept-shift', strategySeed, 'shifts=2;label=1', True, None),
        # ('u-ul', 'unbalancedness', strategySeed, 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', True, None),

        # ### for dataset: 28_fashion-mnist_train.csv
        # ('iid-u', 'uniform', strategySeed, '', True, None),
        # ('cs-dao', 'dense-and-outliers', strategySeed, '0.7', True, 'euclidean'),
        # ('cs-dsad', 'covariate-shift', strategySeed, 'attribute=20;shift=0.3;splits=3;method=0', True, None),
        # ('pps-s', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', True, None),
        # ('pps-ab', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=46;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', True, None),
        # ('pps-mpa', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=47;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', True, None),
        # ('pps-ms', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-3;emptyWorkerFill=44;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', True, None),
        # ('pps-mps', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=2;emptyWorkerFill=45;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', True, None),
        # ('cd-sba', 'concept-drift', strategySeed, 'drifts=3;discreteRanges=40;label=2', True, None),
        # ('cs-cl', 'concept-shift', strategySeed, 'shifts=2;label=2', True, None),
        # ('u-ul', 'unbalancedness', strategySeed, 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', True, None),

        # ### for dataset: shuttle-train.csv
        # ('iid-u', 'uniform', strategySeed, '', True, None),
        # ('cs-dao', 'dense-and-outliers', strategySeed, '0.7', True, 'euclidean'),
        # ('cs-dsad', 'covariate-shift', strategySeed, 'attribute=7;shift=0.3;splits=3;method=0', True, None),
        # ('pps-s', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', True, None),
        # ('pps-ab', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=6;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', True, None),
        # ('pps-mpa', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=7;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', True, None),
        # ('pps-ms', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-3;emptyWorkerFill=4;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', True, None),
        # ('pps-mps', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=2;emptyWorkerFill=5;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', True, None),
        # ('cd-sba', 'concept-drift', strategySeed, 'drifts=3;discreteRanges=40;label=1', True, None),
        # ('cs-cl', 'concept-shift', strategySeed, 'shifts=2;label=1', True, None),
        # ('u-ul', 'unbalancedness', strategySeed, 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', True, None),

        # ### for dataset: adult-train_numeric.csv
        # ('iid-u', 'uniform', strategySeed, '', True, None),
        # ('cs-dao', 'dense-and-outliers', strategySeed, '0.7', True, 'euclidean'),
        # ('cs-dsad', 'covariate-shift', strategySeed, 'attribute=2;shift=0.3;splits=3;method=0', True, None),
        # ('pps-s', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', True, None),
        # ('pps-ab', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=1;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', True, None),
        # ('pps-mpa', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=2;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', True, None),
        # ('pps-ms', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=1;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', True, None),
        # ('pps-mps', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', True, None),
        # ('cd-sba', 'concept-drift', strategySeed, 'drifts=3;discreteRanges=40;label=0', True, None),
        # ('cs-cl', 'concept-shift', strategySeed, 'shifts=2;label=0', True, None),
        # ('u-ul', 'unbalancedness', strategySeed, 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', True, None),

        # ### for dataset: cure-t1-2000n-2D_plus50k.csv
        # ('iid-u', 'uniform', strategySeed, '', True, None),
        # ('cs-dao', 'dense-and-outliers', strategySeed, '0.7', True, 'euclidean'),
        # ('cs-dsad', 'covariate-shift', strategySeed, 'attribute=0;shift=0.3;splits=3;method=0', True, None),
        # ('pps-s', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', True, None),
        # ('pps-ab', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=5;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', True, None),
        # ('pps-mpa', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=6;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', True, None),
        # ('pps-ms', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-3;emptyWorkerFill=3;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', True, None),
        # ('pps-mps', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=2;emptyWorkerFill=4;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', True, None),
        # ('cd-sba', 'concept-drift', strategySeed, 'drifts=3;discreteRanges=40;label=0', True, None),
        # ('cs-cl', 'concept-shift', strategySeed, 'shifts=2;label=0', True, None),
        # ('u-ul', 'unbalancedness', strategySeed, 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', True, None),

        # ### for dataset: diamond9_plus50k.csv
        # ('iid-u', 'uniform', strategySeed, '', True, None),
        # ('cs-dao', 'dense-and-outliers', strategySeed, '0.7', True, 'euclidean'),
        # ('cs-dsad', 'covariate-shift', strategySeed, 'attribute=0;shift=0.3;splits=3;method=0', True, None),
        # ('pps-s', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', True, None),
        # ('pps-ab', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=8;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', True, None),
        # ('pps-mpa', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=9;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', True, None),
        # ('pps-ms', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-3;emptyWorkerFill=6;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', True, None),
        # ('pps-mps', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=2;emptyWorkerFill=7;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', True, None),
        # ('cd-sba', 'concept-drift', strategySeed, 'drifts=3;discreteRanges=40;label=4', True, None),
        # ('cs-cl', 'concept-shift', strategySeed, 'shifts=2;label=4', True, None),
        # ('u-ul', 'unbalancedness', strategySeed, 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', True, None),

        # ### for dataset: synthetic-kdd-like-7g-100k_over.csv
        # ('iid-u', 'uniform', strategySeed, '', True, None),
        # ('cs-dao', 'dense-and-outliers', strategySeed, '0.7', True, 'euclidean'),
        # ('cs-dsad', 'covariate-shift', strategySeed, 'attribute=0;shift=0.3;splits=3;method=0', True, None),
        # ('pps-s', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', True, None),
        # ('pps-ab', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=6;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', True, None),
        # ('pps-mpa', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=7;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', True, None),
        # ('pps-ms', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-3;emptyWorkerFill=4;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', True, None),
        # ('pps-mps', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=2;emptyWorkerFill=5;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', True, None),
        # ('cd-sba', 'concept-drift', strategySeed, 'drifts=3;discreteRanges=40;label=0', True, None),
        # ('cs-cl', 'concept-shift', strategySeed, 'shifts=2;label=0', True, None),
        # ('u-ul', 'unbalancedness', strategySeed, 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', True, None),

        # ### for dataset: synthetic-kdd-like-12g-100k.csv
        # ('iid-u', 'uniform', strategySeed, '', True, None),
        # ('cs-dao', 'dense-and-outliers', strategySeed, '0.7', True, 'euclidean'),
        # ('cs-dsad', 'covariate-shift', strategySeed, 'attribute=0;shift=0.3;splits=3;method=0', True, None),
        # ('pps-s', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=0;emptyWorkerFill=1;fillEmptyButPercent=0.4;additionalClassesPercent=0', True, None),
        # ('pps-ab', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-1;emptyWorkerFill=11;fillEmptyButPercent=0.5;additionalClassesPercent=0.1', True, None),
        # ('pps-mpa', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=12;emptyWorkerFill=0;fillEmptyButPercent=0.6;additionalClassesPercent=0.05', True, None),
        # ('pps-ms', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=-3;emptyWorkerFill=9;fillEmptyButPercent=0.6;additionalClassesPercent=0.1', True, None),
        # ('pps-mps', 'most-of-one-plus-some', strategySeed, 'additionalClassesNumber=2;emptyWorkerFill=10;fillEmptyButPercent=0.8;additionalClassesPercent=0.05', True, None),
        # ('cd-sba', 'concept-drift', strategySeed, 'drifts=3;discreteRanges=40;label=11', True, None),
        # ('cs-cl', 'concept-shift', strategySeed, 'shifts=2;label=11', True, None),
        # ('u-ul', 'unbalancedness', strategySeed, 'unbalancedness=0.1;nodeThreshold=2;proportional=0;', True, None),
    ]

    # FIXME setup schedule one-node sample algorithms
    # onenode
    # wekaKmeans = loadJar('./samples/k-means-weka.jar', False)
    # wekaSvm = loadJar('./samples/svm-weka.jar', False)

    # FIXME setup schedule multinode-node sample clustering algorithms
    # multinode - clustering
    dkm = loadJar('./samples/dkmeans.jar', False)
    # aoptkm = loadJar('./samples/aoptkm.jar', False)
    # lct = loadJar('./samples/lct.jar', False)
    # dbirch = loadJar('./samples/dbirch.jar', False)

    # FIXME setup schedule multinode-node sample classification algorithms
    # multinode - classification
    # bayes = loadJar('./samples/naive-bayes.jar', False)
    # svm2lvl = loadJar('./samples/svm-2lvl.jar', False)
    # dmeb = loadJar('./samples/dmeb.jar', False)
    # dmeb2 = loadJar('./samples/dmeb-2.jar', False)

    # FIXME setup schedule default set of parameters to override
    # default params
    kernel = 'rbf'
    groups = 3
    clusteringDefaultParams = {'groups': groups,
                               'iterations': '20',
                               'epsilon': '0.002',
                               'branching_factor': '50',
                               'threshold': '0.01',
                               'g_groups': groups,
                               'g_threshold': '0.01',
                               'b': '2',
                               'noOneGroup': 'false',
                               'minKGroups': 'true',
                               'exactKGroups': 'false',
                               'init_kmeans_method': 'k-means++',
                               'distanceFunctionName': 'euclidean'}
    classificationDefaultParams = {'kernel': kernel,
                                   'meb_clusters': '-1',
                                   'knn_k': '-1',
                                   'use_local_classifier': 'false',
                                   'use_first_level_only': 'false',
                                   'global_normalization': 'false',
                                   'random_percent': '-0.1',
                                   'close_to_percent': '-2',
                                   'global_expand_percent': '-0.1',
                                   'local_method_for_svs_clusters': 'close_to',
                                   'local_method_for_non_multiclass_clusters': 'random',
                                   'local_method_name': 'only_with_svs',
                                   'first_level_classification_result': 'true'}

    # FIXME setup schedule default execution multiplier
    multiply = 3
    executionsMultiplied = [  # alg, params, distance func. name, distance func. id, isMultinode
                               # (wekaKmeans, {'groups': groups, 'iterations': '20', 'preCalcCentroids': 'true'}, 'euclidean', None, False),
                               # (wekaSvm, {'kernel': kernel}, 'euclidean', None, False),
                               # (bayes, dict({}, **{}), 'euclidean', None, False),
                               # (dbirch, dict(clusteringDefaultParams, **{}), 'euclidean', None, False),

                               # (bayes, dict({}, **{}), 'euclidean', None, True),
                               # (svm2lvl, dict(classificationDefaultParams, **{}), 'euclidean', None, True),
                               (dkm, dict(clusteringDefaultParams, **{}), 'euclidean', None, True),
                               # (aoptkm, dict(clusteringDefaultParams, **{}), 'euclidean', None, True),
                               # (lct, dict(clusteringDefaultParams, **{}), 'euclidean', None, True),
                               # (dbirch, dict(clusteringDefaultParams, **{}), 'euclidean', None, True),

                               # FIXME setup schedule sample extend & override default params
                               # (aoptkm, dict(clusteringDefaultParams, **{'minKGroups': 'true', 'noOneGroup': 'false'}), 'euclidean', None, True), # shuttle
                           ] * multiply
    executions = executionsMultiplied

    # check data
    for d in data:
        checkExist('data', d[0])
        checkExist('data', d[1])
    # check algorithms
    for execution in executions:
        checkExist('alg', execution[0])
    # strategy may be included - not loaded

    debug = False

    iter = 1
    size = len(data) * len(instances) * len(strategies) * len(executions)
    for instance in instances:
        oneNodeInstance = instance[0] == 1
        print('  instance:', instance, 'onenode:', oneNodeInstance)
        workers = instance[0]
        cpu = instance[1]
        workerMemory = instance[2]
        masterMemory = instance[3]
        disk = 10  # not really used for dockers

        print('  Clearing previous', end='', flush=True)
        destroyAll(debug)
        instanceId = createInstance(workers, cpu, workerMemory, masterMemory, disk, debug)

        print(' and wait for setup', end='', flush=True)
        while instanceStatus(instanceId, False) != 200:
            print('.', end='', flush=True)
            time.sleep(2)
        print('')
        instanceConfigUpdate(instanceId, debug)

        for d in data:
            print(' data:', d)
            trainDataId = d[0]
            testDataId = d[1]

            for strategy in strategies:
                print('   strategy:', strategy)
                strategyAlias = strategy[0]
                strategyName = strategy[1]
                strategySeed = strategy[2]
                strategyParams = strategy[3]
                multiNodeStrategy = strategy[4]
                distanceFunction = strategy[5]

                if oneNodeInstance and multiNodeStrategy:
                    print('   [I] multinode partitioning strategy requires multiple nodes, so omit')
                    iter += len(strategies)
                    continue
                elif not oneNodeInstance and not multiNodeStrategy:
                    print('   [I] non-multinode partitioning strategy requires single node, so omit')
                    iter += len(strategies)
                    continue

                scatterResp = scatterData(instanceId, trainDataId, strategyName, strategyParams, distanceFunction,
                                          'train', strategySeed, debug)
                if checkIsResponseError(scatterResp):
                    print('   [E] scatter train data failed', scatterResp)
                    iter += len(executions)
                    continue
                if testDataId:
                    scatterResp = scatterData(instanceId, testDataId, 'dummy', None, None, 'test', strategySeed, debug)
                    if checkIsResponseError(scatterResp):
                        print('   [E] scatter test data failed', scatterResp)
                        iter += len(executions)
                        continue

                for execution in executions:
                    print('    execution:', execution)
                    algorithmId = execution[0]
                    executionParams = execution[1]
                    distanceFunctionName = execution[2]
                    distanceFunctionId = execution[3]
                    multiNodeStrategy = execution[4]

                    executionParams['groups'] = d[2]
                    executionParams['g_groups'] = d[2]

                    if executionSeed and 'seed' not in executionParams:
                        executionParams['seed'] = executionSeed

                    if oneNodeInstance and multiNodeStrategy:
                        print('    [I] distributed algorithm requires multiple nodes, so ommit')
                        iter += 1
                        continue
                    elif not oneNodeInstance and not multiNodeStrategy:
                        print('    [I] local algorithm requires one node, so ommit')
                        iter += 1
                        continue
                    if multiNodeStrategy and strategyAlias in avoidMultiNodeStrategiesAlias:
                        print('    [I] avoiding "' + strategyAlias + '" strategy for multi node, so ommit')
                        iter += 1
                        continue

                    broadcastJar(instanceId, algorithmId, debug)
                    executionId = None
                    while not executionId or 'Error' in executionId or 'Exception' in executionId:
                        try:
                            executionId = startExecution(instanceId, algorithmId, trainDataId, testDataId,
                                                         distanceFunctionName, executionParams, debug)
                        except:
                            print('       [E] !!!! Strange start exec error: ' + str(sys.exc_info()[0]))

                    progress = '[' + str(iter).rjust(len(str(size))) + ' / ' + str(size) + ']'
                    iter += 1
                    print('    ' + progress + ' Wait for finish of: ' + executionId + ' ', end='', flush=True)
                    status = 'UNKNOWN'
                    message = None
                    while status != 'FINISHED' and status != 'FAILED' and status != 'STOPPED':
                        time.sleep(5)
                        try:
                            execStatus = executionStatus(executionId, False)
                            status = execStatus['status']
                            message = execStatus['message']
                        except KeyError:
                            status = 'UNKNOWN'
                        print('.', end='', flush=True)
                    print('')

                    if status != 'FINISHED':
                        print('        [E] FAILED: ' + str(message))
                        iter += 1
                        continue

                    metrics = validateResults(executionId, 'accuracy,recall,precision,f-measure,ARI',
                                              debug)  # AMI - long time calculation when more data
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
                    headers.append('first-level-only')
                    headers.append('multi-method')
                    headers.append('single-method')
                    headers.append('random-percent')
                    headers.append('close-to-percent')
                    headers.append('global-expand-percent')
                    headers.append('ARI')
                    headers.append('AMI')
                    headers.append('f-measure')
                    headers.append('accuracy')
                    headers.append('recall')
                    headers.append('precision')
                    headers.append('ddmTotalTrainingProcessing')
                    headers.append('sentSamples')
                    headers.append('allSamples')
                    headers.append('localBytes')
                    headers.append('globalBytes')
                    headers.append('globalMethodBytes')
                    headers.append('trainingBytes')
                    headers.append('ddmTotalProcessing')
                    headers.append('ddmTotalTrainingProcessing')
                    headers.append('ddmTotalValidationProcessing')
                    headers.append('localProcessing')
                    headers.append('globalProcessing')
                    headers.append('maxLocalProcessing')
                    headers.append('maxGlobalProcessing')
                    headers.append('localLoading')
                    headers.append('maxLocalLoading')
                    headers.append('executionLoading')
                    headers.append('maxExecutionLoading')
                    headers.append('total')
                    headers.append('totalMaxProcessing')
                    headers.append('totalExecution')
                    headers.append('maxExecution')
                    headers.append('totalWithoutLoading')
                    headers.append('totalExecutionWithoutLoading')
                    headers.append('ddmTotalWithoutMaxLoadings')
                    print('         CSV_READY_HEADER', ';'.join(headers))
                    values = []
                    values.append(checkExist('data', trainDataId)['originalName'])
                    values.append(workers)
                    values.append(strategyAlias)
                    values.append(str(strategyParams).replace(';', '|'))

                    algAliases = {
                        'Naive Bayes': 'naive-bayes',
                        'WEKA SVM': 'svm',
                        'WEKA K-means': 'k-means',
                        'BIRCH': 'birch',
                        'SVN-2-LVL': 'dn-svm',
                        'DK-means': 'dk-means',
                        'Opt-DKM': 'opt-dkm',
                        'LCT': 'lct',
                        'Distributed BIRCH': 'dp-birch'
                    }
                    algName = checkExist('alg', algorithmId)['algorithmName']
                    values.append(('s_' if oneNodeInstance else 'd_') + algAliases[algName])

                    values.append(str(executionParams).replace(';', '|'))
                    values.append(executionParams.get('kernel', ''))
                    values.append(executionParams.get('meb_clusters', ''))
                    values.append(executionParams.get('use_local_classifier', ''))
                    values.append(executionParams.get('use_first_level_only', ''))
                    values.append(executionParams.get('local_method_for_svs_clusters', ''))
                    values.append(executionParams.get('local_method_for_non_multiclass_clusters', ''))
                    values.append(executionParams.get('random_percent', ''))
                    values.append(executionParams.get('close_to_percent', ''))
                    values.append(executionParams.get('global_expand_percent', ''))
                    values.append(metrics.get('ARI', ''))
                    values.append(metrics.get('AMI', ''))
                    values.append(metrics.get('f-measure', ''))
                    values.append(metrics.get('accuracy', ''))
                    values.append(metrics.get('recall', ''))
                    values.append(metrics.get('precision', ''))
                    values.append(stats['time']['ddmTotalTrainingProcessing'])
                    values.append(handleCustomMetrics(stats['custom']))
                    values.append(stats['transfer']['localBytes'])
                    values.append(stats['transfer']['globalBytes'])
                    values.append(stats['transfer']['globalMethodBytes'])
                    values.append(stats['data']['trainingBytes'])
                    values.append(stats['time']['ddmTotalProcessing'])
                    values.append(stats['time']['ddmTotalTrainingProcessing'])
                    values.append(stats['time']['ddmTotalValidationProcessing'])
                    values.append(stats['time']['localProcessing'])
                    values.append(stats['time']['globalProcessing'])
                    values.append(stats['time']['maxLocalProcessing'])
                    values.append(stats['time']['maxGlobalProcessing'])
                    values.append(stats['time']['localLoading'])
                    values.append(stats['time']['maxLocalLoading'])
                    values.append(stats['time']['executionLoading'])
                    values.append(stats['time']['maxExecutionLoading'])
                    values.append(stats['time']['total'])
                    values.append(stats['time']['totalMaxProcessing'])
                    values.append(stats['time']['totalExecution'])
                    values.append(stats['time']['maxExecution'])
                    values.append(stats['time']['totalWithoutLoading'])
                    values.append(stats['time']['totalExecutionWithoutLoading'])
                    values.append(stats['time']['ddmTotalWithoutMaxLoadings'])
                    values = list(map(str, values))
                    print('         CSV_READY_VALUES', ';'.join(values))
    print('  Clearing instances', flush=True)
    destroyAll(debug)

    print('')
    print('SCHEDULE FINISHED')


def handleCustomMetrics(metrics):
    try:
        locals = metrics['locals']
        globals = metrics['globals']
        if not locals and not globals:
            return 'n/a'

        transferred = 0
        all = 0
        for lm in locals:
            for key, lmval in lm.items():
                if lmval == 'null':
                    continue

                values = re.split('/|%', lmval)
                transferred += int(values[0])
                if len(values) > 1:
                    all += int(values[1])

        for gm in globals:
            if gm == 'null':
                continue

            values = re.split('/|%', gm)
            transferred += int(values[0])
            if len(values) > 1:
                all += int(values[1])

        return str(transferred) + ';' + str(all)
    except:
        return str(sys.exc_info()[0]) + ';'


if len(sys.argv) < 2:
    print(
        '  Provide command! [setup, inststatus, confupdate, clear, reload, execute, status, logs, lastlog, results, validate, stats, schedule, partition, info [data, alg, func, strgy, exec, inst]]')
    sys.exit(1)

command = sys.argv[1]
oneNode = False
if len(sys.argv) > 2 and sys.argv[2] == 'onenode':
    oneNode = True

if command == 'setup':
    if oneNode:
        # FIXME setup default single node instance parameters
        setupDefault(1, 2, 4, 4, True)
    else:
        # FIXME setup default multi node instance parameters
        setupDefault(4, 2, 3, 3, False)
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
elif command == 'partition':
    if len(sys.argv) < 7:
        print('  Provide info args: partitions, strategy, strategyParams, distanceFunction, seed')
        sys.exit(1)
    partition(sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[6], oneNode)
else:
    print('  Unknown command')
