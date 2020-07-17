package pl.edu.pw.ddm.platform.core.data;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pl.edu.pw.ddm.platform.core.data.dto.DataDescDto;
import pl.edu.pw.ddm.platform.core.data.dto.DistanceFunctionDescDto;
import pl.edu.pw.ddm.platform.interfaces.data.strategy.PartitionerStrategy;

@Mapper
public interface DataDescMapper {

    DataDescMapper INSTANCE = Mappers.getMapper(DataDescMapper.class);

    @Mapping(target = "filesLocations", source = "location.filesLocations")
    @Mapping(target = "sizesInBytes", source = "location.sizesInBytes")
    @Mapping(target = "numbersOfSamples", source = "location.numbersOfSamples")
    DataDescDto map(DataLoader.DataDesc desc);

    @Mapping(target = "filesLocations", source = "location.filesLocations")
    @Mapping(target = "sizesInBytes", source = "location.sizesInBytes")
    @Mapping(target = "numbersOfSamples", source = "location.numbersOfSamples")
    PartitionerStrategy.DataDesc mapStrategy(DataLoader.DataDesc desc);

    DistanceFunctionDescDto map(DistanceFunctionLoader.DistanceFunctionDesc desc);

}
