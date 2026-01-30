package com.tnh.baseware.core.mappers.user;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.audit.CategoryDTO;
import com.tnh.baseware.core.dtos.user.MenuDTO;
import com.tnh.baseware.core.entities.user.Menu;
import com.tnh.baseware.core.forms.user.MenuEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.audit.ICategoryRepository;
import org.mapstruct.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IMenuMapper extends IGenericMapper<Menu, MenuEditorForm, MenuDTO> {

    @Mapping(target = "menuType", expression = "java(fetcher.formToEntity(repository, form.getMenuTypeId()))")
    Menu formToEntity(MenuEditorForm form,
            @Context GenericEntityFetcher fetcher,
            @Context ICategoryRepository repository);

    @Mapping(target = "menuType", expression = "java(fetcher.formToEntity(repository, form.getMenuTypeId()))")
    void updateMenuFromForm(MenuEditorForm form,
            @MappingTarget Menu menu,
            @Context GenericEntityFetcher fetcher,
            @Context ICategoryRepository repository);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "mapParent")
    MenuDTO entityToDTO(Menu entity);

    @Named("mapParent")
    default MenuDTO mapParent(Menu parent) {
        if (parent == null)
            return null;

        var parentDTO = MenuDTO.builder()
                .id(parent.getId())
                .title(parent.getTitle())
                .alias(parent.getAlias())
                .note(parent.getNote())
                .path(parent.getPath())
                .link(parent.getLink())
                .published(parent.getPublished())
                .browserNav(parent.getBrowserNav())
                .icon(parent.getIcon())
                .menuOrder(parent.getMenuOrder())
                .description(parent.getDescription());

        var menuType = parent.getMenuType();
        if (menuType != null) {
            var menuTypeDTO = CategoryDTO.builder()
                    .id(menuType.getId())
                    .code(menuType.getCode())
                    .name(menuType.getName())
                    .displayName(menuType.getDisplayName())
                    .description(menuType.getDescription())
                    .build();
            parentDTO.menuType(menuTypeDTO);
        }

        return parentDTO.build();
    }

    default List<MenuDTO> mapMenusToTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty())
            return List.of();

        var parentMap = menus.stream()
                .filter(m -> m.getParent() != null)
                .collect(Collectors.groupingBy(m -> m.getParent().getId()));

        return menus.stream()
                .filter(m -> m.getParent() == null)
                .sorted(Comparator.comparing(Menu::getMenuOrder))
                .map(m -> buildMenuTree(m, parentMap))
                .toList();
    }

    default MenuDTO buildMenuTree(Menu m, Map<UUID, List<Menu>> parentMap) {
        var dto = entityToDTO(m);
        List<Menu> children = parentMap.getOrDefault(m.getId(), List.of());

        if (!children.isEmpty()) {
            var childDTOs = children.stream()
                    .sorted(Comparator.comparing(Menu::getMenuOrder))
                    .map(child -> buildMenuTree(child, parentMap))
                    .toList();
            dto.setChildren(childDTOs);
        }

        return dto;
    }
}
