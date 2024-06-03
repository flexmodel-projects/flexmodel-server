package tech.wetech.flexmodel.application.dto;

import java.util.List;

/**
 * @author cjbi
 */
public record PageDTO<T>(List<T> list, Long total) {
}
