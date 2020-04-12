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
        'scatter': '/coordinator/command/data/scatter/{instanceId}/{strategy}/{dataId}',
    },
    'execution': {
        'collectResults': '/coordinator/command/execution/results/collect/{executionId}',
        'info': '/coordinator/command/execution/info',
        'start': '/coordinator/command/execution/start/{instanceId}/{algorithmId}/{dataId}/{distanceFunctionIdOrPredefinedName}',
        'status': '/coordinator/command/execution/status/executionId}',
        'stop': '/coordinator/command/execution/stop/{executionId}'
    },
    'instance': {
        'create': '/coordinator/command/instance/create/{workers}',
        'destroy': '/coordinator/command/instance/destroy/{instanceId}',
        'destroyAll': '/coordinator/command/instance/destroy/all',
        'info': '/coordinator/command/instance/info',
        'addresses': '/coordinator/command/instance/info/{instanceId}'
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


def scatterData(instanceId, dataId, strategy='uniform'):
    print("scatterData instanceId='{}' dataId='{}' strategy='{}'".format(instanceId, dataId, strategy))
    url = baseUrl + api['data']['scatter'].format(**{
        'instanceId': instanceId,
        'dataId': dataId,
        'strategy': strategy
    })
    response = requests.get(url).text
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


def startExecution(instanceId, algorithmId, dataId, distanceFuncName='none'):
    print("startExecution instanceId='{}' algorithmId='{}' dataId='{}' distanceFuncName='{}'".format(instanceId,
                                                                                                     algorithmId,
                                                                                                     dataId,
                                                                                                     distanceFuncName))
    url = baseUrl + api['execution']['start'].format(**{
        'instanceId': instanceId,
        'algorithmId': algorithmId,
        'dataId': dataId,
        'distanceFunctionIdOrPredefinedName': distanceFuncName
    })
    executionId = requests.get(url).text
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


def saveLast(instanceId, algorithmId, dataId, distanceFunctionId=None, executionId=None):
    config = configparser.RawConfigParser()
    config['last'] = {
        'instance_id': instanceId,
        'algorithm_id': algorithmId,
        'data_id': dataId,
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
    dataId = loadData('./samples/iris.data', 4, ',', None)
    distanceFunctionId = loadDistanceFunction('./samples/equality.jar')
    instanceId = createInstance(workers)

    time.sleep(workers * 5)
    broadcastJar(instanceId, algorithmId)
    scatterData(instanceId, dataId, 'uniform')
    broadcastDistanceFunction(instanceId, distanceFunctionId)

    saveLast(instanceId, algorithmId, dataId, distanceFunctionId)


def reload():
    last = loadLast()
    instanceId = last.get('instance_id')

    algorithmId = loadJar('./samples/aoptkm.jar')
    dataId = loadData('./samples/iris.data', 4, ',', None)
    distanceFunctionId = loadDistanceFunction('./samples/equality.jar')

    broadcastJar(instanceId, algorithmId)
    scatterData(instanceId, dataId, 'uniform')
    broadcastDistanceFunction(instanceId, distanceFunctionId)

    saveLast(instanceId, algorithmId, dataId, distanceFunctionId)


def execute():
    last = loadLast()
    instanceId = last.get('instance_id')
    algorithmId = last.get('algorithm_id')
    dataId = last.get('data_id')
    distanceFunctionId = last.get('distance_function_id')

    executionId = startExecution(instanceId, algorithmId, dataId)
    # executionId = startExecution(instanceId, algorithmId, dataId, distanceFunctionId)

    saveLast(instanceId, algorithmId, dataId, distanceFunctionId, executionId)


def results():
    last = loadLast()
    collectResults(last.get('execution_id'))


def clear():
    destroyAll()


if len(sys.argv) < 2:
    print('  Provide command! [setup, clear, reload, execute, info [data, alg, execution, instance]]')
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
