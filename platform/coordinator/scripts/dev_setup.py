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
        'load': '/coordinator/command/data/load/file',
        'loadUri': '/coordinator/command/data/load/uri',
        'scatter': '/coordinator/command/data/scatter/{instanceId}/{dataId}',
    },
    'execution': {
        'collectResults': '/coordinator/command/execution/results/collect/{executionId}',
        'info': '/coordinator/command/execution/info',
        'start': '/coordinator/command/execution/start/{instanceId}/{algorithmId}/{trainDataId}',
        'status': '/coordinator/command/execution/status/executionId}',
        'stop': '/coordinator/command/execution/stop/{executionId}'
    },
    'instance': {
        'create': '/coordinator/command/instance/create/{workers}',
        'destroy': '/coordinator/command/instance/destroy/{instanceId}',
        'destroyAll': '/coordinator/command/instance/destroy/all',
        'info': '/coordinator/command/instance/info',
        'addresses': '/coordinator/command/instance/info/{instanceId}'
    },
    'results': {
        'validate': '/coordinator/command/results/validate/{executionId}'
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


def functionsInfo():
    print('functionsInfo')
    url = baseUrl + api['data']['distance-functions-info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def loadData(path, labelIndex, separator=',', idIndex=None):
    print("loadData path='{}' idIndex='{}' labelIndex='{}' separator='{}'".format(path, idIndex, labelIndex, separator))
    url = baseUrl + api['data']['load']
    with open(path, 'rb') as file:
        dataId = requests.post(url,
                               files={'dataFile': (file.name, file, 'application/x-java-archive')},
                               data={
                                   'idIndex': idIndex,
                                   'labelIndex': labelIndex,
                                   'separator': separator
                               }
                               ).text
        print('  dataId: ' + dataId)
        return dataId


def scatterData(instanceId, dataId, strategy='uniform', typeCode='train'):
    print("scatterData instanceId='{}' dataId='{}' strategy='{}' typeCode='{}'".format(instanceId, dataId, strategy,
                                                                                       typeCode))
    url = baseUrl + api['data']['scatter'].format(**{
        'instanceId': instanceId,
        'dataId': dataId
    })
    response = requests.post(url,
                             data={
                                 'strategy': strategy,
                                 'typeCode': typeCode
                             }
                             ).text
    print('  response: ' + response)
    return response


def collectResults(executionId):
    print("collectResults executionId='{}'".format(executionId))
    url = baseUrl + api['execution']['collectResults'].format(**{'executionId': executionId})
    response = requests.get(url).text
    print('  response: ' + response)
    return response


def executionInfo():
    print('executionInfo')
    url = baseUrl + api['execution']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


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
        'groups': '3',
        'iterations': '20',
        'epsilon': '0.002'
    })
    executionId = requests.post(url,
                                data={
                                    'testDataId': testDataId,
                                    'distanceFunctionName': distanceFuncName,
                                    # 'distanceFunctionId': '1156746230', # loaded equality
                                    'executionParams': jsonParams
                                }
                                ).text
    print('  executionId: ' + executionId)
    return executionId


def createInstance(workers):
    print("createInstance workers='{}'".format(workers))
    url = baseUrl + api['instance']['create'].format(**{'workers': workers})
    instanceId = requests.get(url).text
    print('  instanceId: ' + instanceId)
    return instanceId


def instanceInfo():
    print('instanceInfo')
    url = baseUrl + api['instance']['info']
    response = requests.get(url).text
    formatted = json.loads(response)
    pprint.pprint(formatted)


def destroyAll():
    print('destroyAll')
    url = baseUrl + api['instance']['destroyAll']
    response = requests.get(url).text
    print('  response: ' + response)
    return response


def validateResults(executionId, metrics):
    print("validateResults executionId='{}' metrics='{}'".format(executionId, metrics))
    url = baseUrl + api['results']['validate'].format(**{'executionId': executionId})
    response = requests.post(url,
                             data={'metrics': metrics}
                             ).text
    print('  response: ' + response)
    return response


def saveLast(instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId=None, executionId=None):
    config = configparser.RawConfigParser()
    config['last'] = {
        'instance_id': instanceId,
        'algorithm_id': algorithmId,
        'train_data_id': trainDataId,
        'test_data_id': testDataId,
        'distance_function_id': distanceFunctionId,
        'execution_id': executionId
    }
    with open(lastExecFile, 'w') as file:
        config.write(file)


def loadLast():
    config = configparser.RawConfigParser()
    config.read(lastExecFile)
    return dict(config['last'])


def setupDefault(workers=2):
    algorithmId = loadJar('./samples/aoptkm.jar')
    # algorithmId = loadJar('./samples/random-classifier.jar')
    trainDataId = loadData('./samples/iris.data', 4, ',', None)
    testDataId = loadData('./samples/iris.test', 4, ',', None)
    distanceFunctionId = loadDistanceFunction('./samples/equality.jar')
    instanceId = createInstance(workers)

    time.sleep(workers * 5)
    broadcastJar(instanceId, algorithmId)
    scatterData(instanceId, trainDataId, 'uniform', 'train')
    scatterData(instanceId, testDataId, 'uniform', 'test')
    broadcastDistanceFunction(instanceId, distanceFunctionId)

    saveLast(instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId)


def reload():
    last = loadLast()
    instanceId = last.get('instance_id')

    algorithmId = loadJar('./samples/aoptkm.jar')
    # algorithmId = loadJar('./samples/random-classifier.jar')
    trainDataId = loadData('./samples/iris.data', 4, ',', None)
    testDataId = loadData('./samples/iris.test', 4, ',', None)
    distanceFunctionId = loadDistanceFunction('./samples/equality.jar')

    broadcastJar(instanceId, algorithmId)
    scatterData(instanceId, trainDataId, 'uniform', 'train')
    scatterData(instanceId, testDataId, 'uniform', 'test')
    broadcastDistanceFunction(instanceId, distanceFunctionId)

    saveLast(instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId)


def execute():
    last = loadLast()
    instanceId = last.get('instance_id')
    algorithmId = last.get('algorithm_id')
    trainDataId = last.get('train_data_id')
    testDataId = last.get('test_data_id')
    distanceFunctionId = last.get('distance_function_id')

    executionId = startExecution(instanceId, algorithmId, trainDataId, testDataId)
    # executionId = startExecution(instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId)

    saveLast(instanceId, algorithmId, trainDataId, testDataId, distanceFunctionId, executionId)


def results():
    last = loadLast()
    collectResults(last.get('execution_id'))


def validate():
    last = loadLast()
    validateResults(last.get('execution_id'), 'ARI')


def clear():
    destroyAll()


if len(sys.argv) < 2:
    print('  Provide command! [setup, clear, reload, execute, validate, info [data, alg, execution, instance]]')
    sys.exit(1)

command = sys.argv[1]
if command == 'setup':
    setupDefault()
elif command == 'clear':
    clear()
elif command == 'reload':
    reload()
elif command == 'execute':
    execute()
elif command == 'results':
    results()
elif command == 'validate':
    validate()
elif command == 'info':
    if len(sys.argv) < 3:
        print('  Provide info arg [data, functions, alg, execution, instance]')
        sys.exit(1)

    arg = sys.argv[2]
    if arg == 'data':
        dataInfo()
    if arg == 'functions':
        functionsInfo()
    elif arg == 'alg':
        algorithmInfo()
    elif arg == 'execution':
        executionInfo()
    elif arg == 'instance':
        instanceInfo()
    else:
        print('  Unknown info to show')
else:
    print('  Unknown command')
