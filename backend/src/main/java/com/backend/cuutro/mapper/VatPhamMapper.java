package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.NhomVatPhamLiteDto;
import com.backend.cuutro.dto.response.entities.VatPhamDto;
import com.backend.cuutro.entities.NhomVatPhamEntity;
import com.backend.cuutro.entities.VatPhamEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {DonViMapper.class, TepTinMapper.class})
public interface VatPhamMapper {

    @Mapping(target = "nhomVatPhams", expression = "java(toNhomVatPhamLiteList(entity.getNhomVatPhams()))")
    VatPhamDto toDto(VatPhamEntity entity);

    @Mapping(target = "nhomVatPhams", ignore = true)
    VatPhamEntity toEntity(VatPhamDto dto);

    List<VatPhamDto> toDtoList(List<VatPhamEntity> entityList);

    List<VatPhamEntity> toEntityList(List<VatPhamDto> dtoList);

    default List<NhomVatPhamLiteDto> toNhomVatPhamLiteList(Set<NhomVatPhamEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        List<NhomVatPhamEntity> sortedEntities = new ArrayList<>(new LinkedHashSet<>(entities));
        sortedEntities.sort(Comparator.comparing(
                NhomVatPhamEntity::getId,
                Comparator.nullsLast(Long::compareTo)));

        List<NhomVatPhamLiteDto> result = new ArrayList<>();
        for (NhomVatPhamEntity entity : sortedEntities) {
            if (entity == null || entity.getId() == null) {
                continue;
            }
            result.add(NhomVatPhamLiteDto.builder()
                    .id(entity.getId())
                    .ten(entity.getTen())
                    .moTa(entity.getMoTa())
                    .createdAt(entity.getCreatedAt())
                    .build());
        }
        return result;
    }

    default Page<VatPhamDto> toDtoPage(Page<VatPhamEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
