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


def loadJar(path):
    print("loadJar path='{}'".format(path))
    url = baseUrl + api['algorithm']['load']
    with open(path, 'rb') as file:
        algorithmId = requests.post(url,
                                    files={'file': (file.name, file, 'application/x-java-archive')}
                                    ).text
        print('  algorithmId: ' + algorithmId)
        return algorithmId


def algorithmInfo():
    print('algorithmInfo')
    url = baseUrl + api['algorithm']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def broadcastJar(instanceId, algorithmId):
    print("broadcastJar instanceId='{}' algorithmId='{}'".format(instanceId, algorithmId))
    url = baseUrl + api['algorithm']['broadcast'].format(**{
        'instanceId': instanceId,
        'algorithmId': algorithmId
    })
    response = requests.get(url).text
    print('  response: ' + response)
    return response


def dataInfo():
    print('dataInfo')
    url = baseUrl + api['data']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def broadcastDistanceFunction(instanceId, distanceFunctionId):
    print("broadcastDistanceFunction instanceId='{}' distanceFunctionId='{}'".format(instanceId, distanceFunctionId))
    url = baseUrl + api['data']['distance-function-broadcast'].format(**{
        'instanceId': instanceId,
        'distanceFunctionId': distanceFunctionId
    })
    response = requests.get(url).text
    print('  response: ' + response)
    return response


def loadDistanceFunction(path):
    print("loadDistanceFunction path='{}'".format(path))
    url = baseUrl + api['data']['distance-function-load']
    with open(path, 'rb') as file:
        distanceFunctionId = requests.post(url, files={'distanceFunctionFile':
                                                           (file.name, file, 'application/x-java-archive')}).text
        print('  distanceFunctionId: ' + distanceFunctionId)
        return distanceFunctionId


def loadPartitioningStrategy(path):
    print("loadPartitioningStrategy path='{}'".format(path))
    url = baseUrl + api['data']['partitioning-strategy-load']
    with open(path, 'rb') as file:
        partitioningStrategyId = requests.post(url, files={'partitioningStrategyFile':
                                                               (file.name, file, 'application/x-java-archive')}).text
        print('  partitioningStrategyId: ' + partitioningStrategyId)
        return partitioningStrategyId


def functionsInfo():
    print('functionsInfo')
    url = baseUrl + api['data']['distance-functions-info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def strategiesInfo():
    print('strategiesInfo')
    url = baseUrl + api['data']['partitioning-strategies-info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def loadData(path, labelIndex, separator=',', idIndex=None, vectorizeStrings=False, percentage=None):
    print("loadData path='{}' idIndex='{}' labelIndex='{}' separator='{}' vectorizeStrings='{}' percentage='{}'".format(
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
        print('  dataId: ' + dataId)
        return dataId


def scatterData(instanceId, dataId, strategy='uniform', strategyParams=None, distanceFunction=None, typeCode='train',
                seed=None):
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
    print('  response: ' + response)
    return response


def executionInfo():
    print('executionInfo')
    url = baseUrl + api['execution']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def collectLogs(executionId):
    print("collectLogs executionId='{}'".format(executionId))
    url = baseUrl + api['execution']['collectLogs'].format(**{'executionId': executionId})
    response = requests.get(url).text
    print('  response: ' + response)
    return response


def fetchLogs(executionId, nodeId, count):
    print("fetchLogs executionId='{}' nodeId='{}' count='{}'".format(executionId, nodeId, count))
    url = baseUrl + api['execution']['fetchLogs'].format(**{
        'executionId': executionId,
        'nodeId': nodeId,
        'count': count
    })
    response = requests.get(url).text
    print('  response size: ' + str(len(response)))
    return response


def collectResults(executionId, debug=True):
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


def startExecution(instanceId, algorithmId, trainDataId, testDataId=None, distanceFuncName='none'):
    print(
        "startExecution instanceId='{}' algorithmId='{}' trainDataId='{}' testDataId='{}' distanceFuncName='{}'".format(
            instanceId,
            algorithmId,
            trainDataId,
            testDataId,
            distanceFuncName))
    url = baseUrl + api['execution']['start'].format(**{
        'instanceId': instanceId,
        'algorithmId': algorithmId,
        'trainDataId': trainDataId
    })
    jsonParams = json.dumps({
        'seed': str(int(round(time.time()))),
        'groups': '3',
        'iterations': '20',
        'epsilon': '0.002',
        'distanceFunctionName': distanceFuncName,
        # 'distanceFunctionId': '1156746230', # loaded equality
        'preCalcCentroids': 'true',
        'b': '2',
        'meb_clusters': '2',
        'kernel': 'linear',
        'knn_k': '3'
    })
    executionId = requests.post(url,
                                data={
                                    'testDataId': testDataId,
                                    'executionParams': jsonParams
                                }
                                ).text
    print('  executionId: ' + executionId)
    return executionId


def executionStatus(executionId, debug=True):
    print("executionStatus executionId='{}'".format(executionId))
    url = baseUrl + api['execution']['status'].format(**{'executionId': executionId})
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def createInstance(workers, cpu=2, memory=2, disk=10):
    print("createInstance workers='{}' cpu='{}' memory='{}' disk='{}'".format(workers, cpu, memory, disk))
    url = baseUrl + api['instance']['create'].format(**{'workers': workers})
    instanceId = requests.post(url,
                               data={
                                   'cpu': cpu,
                                   'memory': memory,
                                   'disk': disk
                               }
                               ).text
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
    print('instanceInfo')
    url = baseUrl + api['instance']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    if debug:
        pprint.pprint(formatted)
    return formatted


def destroyAll():
    print('destroyAll')
    url = baseUrl + api['instance']['destroyAll']
    response = requests.get(url).text
    print('  response: ' + response)
    return response


def validateResults(executionId, metrics):
    print("validateResults executionId='{}' metrics='{}'".format(executionId, metrics))

    if isinstance(collectResults(executionId, False), ValueError):
        return

    url = baseUrl + api['results']['validate'].format(**{'executionId': executionId})
    response = requests.post(url,
                             data={'metrics': metrics}
                             ).text
    print('  response: ' + response)
    return response


def resultsStats(executionId):
    print("resultsStats executionId='{}' ".format(executionId))

    if isinstance(collectResults(executionId, False), ValueError):
        return

    url = baseUrl + api['results']['stats'].format(**{'executionId': executionId})
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


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
        trainDataId = loadData('./samples/iris.data', 4, ',', None, vectorizeStrings, trainPercentage)
        # trainDataId = loadData('./samples/iris_numeric.data', 4, ',', None, vectorizeStrings, trainPercentage)
        testDataId = loadData('./samples/iris.test', 4, ',', None, vectorizeStrings, trainPercentage)
        # testDataId = loadData('./samples/iris_numeric.test', 4, ',', None, vectorizeStrings, trainPercentage)
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
elif command == 'info':
    if len(sys.argv) < 3:
        print('  Provide info arg [data, func, stgry, alg, exec, inst]')
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
