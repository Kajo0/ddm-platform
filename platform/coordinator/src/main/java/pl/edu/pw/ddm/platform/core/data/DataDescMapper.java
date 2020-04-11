package pl.edu.pw.ddm.platform.core.data;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pl.edu.pw.ddm.platform.core.data.dto.DataDescDto;

@Mapper
public interface DataDescMapper {

    DataDescMapper INSTANCE = Mappers.getMapper(DataDescMapper.class);

    @Mapping(target = "filesLocations", source = "location.filesLocations")
    @Mapping(target = "sizesInBytes", source = "location.sizesInBytes")
    @Mapping(target = "numbersOfSamples", source = "location.numbersOfSamples")
    DataDescDto map(DataLoader.DataDesc desc);

}
