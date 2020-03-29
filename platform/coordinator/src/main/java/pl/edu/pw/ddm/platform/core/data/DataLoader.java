package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

interface DataLoader {

    String save(String uri, String separator, Integer idIndex, Integer labelIndex, boolean deductType);

    String save(MultipartFile file, String separator, Integer idIndex, Integer labelIndex, boolean deductType);

    File load(String dataId);

    // TODO move DataDesc to Interface
    LocalDataLoader.DataDesc getDataDesc(String datasetId);

    Map<String, LocalDataLoader.DataDesc> allDataInfo();

}
