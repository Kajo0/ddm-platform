package pl.edu.pw.ddm.platform.core.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pl.edu.pw.ddm.platform.core.instance.InstanceAgentAddressFactory;
import pl.edu.pw.ddm.platform.core.instance.dto.InstanceAddrDto;
import pl.edu.pw.ddm.platform.interfaces.data.DistanceFunction;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;
import pl.edu.pw.ddm.platform.strategies.PartitionerStrategies;
import pl.edu.pw.ddm.platform.strategies.UniformPartitionerStrategy;

@Slf4j
@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class LocalDataPartitioner implements DataPartitioner {

    private final RestTemplate restTemplate;
    private final DistanceFunctionLoader distanceFunctionLoader;
    private final PartitionerStrategyLoader partitionerStrategyLoader;

    @SneakyThrows
    @Override
    public String scatterTrain(List<InstanceAddrDto> addresses, DataLoader.DataDesc dataDesc, String strategy, String distanceFunctionId, String params, Long seed) {
        log.info("Scattering train data '{}' with strategy '{}' and distance func '{}' into nodes '{}' with seed '{}'.", dataDesc, strategy, distanceFunctionId, addresses, seed);
        // TODO if data with same params already scattered - do not send again but think about naming that use only id without params

        List<InstanceAddrDto> workers = addresses.stream()
                .filter(InstanceAddrDto::isWorker)
                .collect(Collectors.toList());

        var fileCreator = new TempFileCreator();
        var strategyParams = PartitionerStrategy.StrategyParameters.builder()
                .partitions(workers.size())
                .customParams(params)
                .distanceFunction(loadDistanceFunction(distanceFunctionId))
                .seed(seed)
                .build();
        List<Path> tempFiles = deductStrategy(strategy)
                .partition(DataDescMapper.INSTANCE.mapStrategy(dataDesc), strategyParams, fileCreator);

        sendDataToNodes(workers, dataDesc, tempFiles, "train");
        fileCreator.cleanup();
        for (Path tempFile : tempFiles) {
            Files.deleteIfExists(tempFile);
        }

        return "ok_process-id";
    }

    @SneakyThrows
    @Override
    public String scatterTestEqually(List<InstanceAddrDto> addresses, DataLoader.DataDesc dataDesc, String distanceFunctionId, Long seed) {
        log.info("Scattering test data '{}' equally into nodes '{}'.", dataDesc, addresses);

        List<InstanceAddrDto> workers = addresses.stream()
                .filter(InstanceAddrDto::isWorker)
                .collect(Collectors.toList());

        var fileCreator = new TempFileCreator();
        var strategyParams = PartitionerStrategy.StrategyParameters.builder()
                .partitions(workers.size())
                .distanceFunction(loadDistanceFunction(distanceFunctionId))
                .customParams(null)
                .seed(seed)
                .build();
        List<Path> tempFiles = new UniformPartitionerStrategy()
                .partition(DataDescMapper.INSTANCE.mapStrategy(dataDesc), strategyParams, fileCreator);

        sendDataToNodes(workers, dataDesc, tempFiles, "test");
        fileCreator.cleanup();
        for (Path tempFile : tempFiles) {
            Files.deleteIfExists(tempFile);
        }

        return "ok_process-id";
    }

    private PartitionerStrategy deductStrategy(String strategy) {
        if (strategy == null) {
            log.info("Null strategy chosen so returning default one: '{}'.", PartitionerStrategies.UNIFORM);
            return new UniformPartitionerStrategy();
        }

        // TODO add more checks
        PartitionerStrategy partitioner = partitionerStrategyLoader.getStrategyImpl(strategy);
        Preconditions.checkNotNull(partitioner, "Unknown strategy id '%s' provided.", strategy);
        return partitioner;
    }

    private DistanceFunction loadDistanceFunction(String distanceFunctionId) {
        if (distanceFunctionId == null) {
            log.debug("No distance function provided.");
            return null;
        } else {
            return distanceFunctionLoader.getDistanceFunctionImpl(distanceFunctionId);
        }
    }

    private void sendDataToNodes(List<InstanceAddrDto> workers, DataLoader.DataDesc dataDesc, List<Path> tempFiles, String typeCode) {
        // TODO optimize
        for (int i = 0; i < workers.size(); ++i) {
            InstanceAddrDto addressDto = workers.get(i);
            Path tempDataFile = tempFiles.get(i);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(1);
            // TODO it may be path as well for FileSystemResource
            body.add("dataFile", new FileSystemResource(tempDataFile));
            body.add("dataId", dataDesc.getId());
            body.add("separator", dataDesc.getSeparator());
            body.add("idIndex", dataDesc.getIdIndex());
            body.add("labelIndex", dataDesc.getLabelIndex());
            body.add("attributesAmount", dataDesc.getAttributesAmount());
            body.add("colTypes", String.join(",", dataDesc.getColTypes()));
            body.add("typeCode", typeCode);

            String url = InstanceAgentAddressFactory.sendData(addressDto);
            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.debug("Scattering data post response: '{}'.", response.getBody());
        }
    }

}
