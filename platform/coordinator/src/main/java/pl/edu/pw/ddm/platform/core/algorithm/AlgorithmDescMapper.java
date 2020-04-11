package pl.edu.pw.ddm.platform.core.algorithm;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import pl.edu.pw.ddm.platform.core.algorithm.dto.AlgorithmDescDto;

@Mapper
public interface AlgorithmDescMapper {

    AlgorithmDescMapper INSTANCE = Mappers.getMapper(AlgorithmDescMapper.class);

    AlgorithmDescDto map(AlgorithmLoader.AlgorithmDesc desc);

}
