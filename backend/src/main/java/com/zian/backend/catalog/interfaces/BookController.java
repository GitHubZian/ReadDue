package com.zian.backend.catalog.interfaces;

import com.zian.backend.catalog.application.BookQueryService;
import com.zian.backend.catalog.interfaces.dto.BookDtos;
import com.zian.backend.shared.common.BaseResponse;
import com.zian.backend.shared.common.PageResult;
import com.zian.backend.shared.common.ResultUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者端书目检索与馆藏接口。
 *
 * <p>四个端点都只读，且只暴露上架、未删除的书目；库存口径统一取视图 {@code v_book_stock}。
 */
@RestController
@RequestMapping("/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookQueryService bookQueryService;

    /**
     * 分页检索书目：书名/作者模糊匹配，ISBN 精确匹配。
     *
     * @param q    关键词，可空；空则返回全部上架书目
     * @param page 页码，从 1 开始
     * @param size 每页条数，上限 200（超过返回 422）
     * @return 分页书目
     */
    @GetMapping
    @PreAuthorize("hasAuthority('catalog:read')")
    public BaseResponse<PageResult<BookDtos.BookItem>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResultUtils.success(bookQueryService.search(q, page, size));
    }

    /**
     * 查询书目详情（含简介、标签与库存）。
     *
     * @param id 书目 ID
     * @return 书目详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('catalog:read')")
    public BaseResponse<BookDtos.BookDetail> detail(@PathVariable Long id) {
        return ResultUtils.success(bookQueryService.detail(id));
    }

    /**
     * 查询书目全部馆藏册（可借优先）。
     *
     * @param id 书目 ID
     * @return 册列表
     */
    @GetMapping("/{id}/copies")
    @PreAuthorize("hasAuthority('catalog:read')")
    public BaseResponse<List<BookDtos.CopyItem>> copies(@PathVariable Long id) {
        return ResultUtils.success(bookQueryService.copies(id));
    }

    /**
     * 查询书目排队人数与本人预约位置。
     *
     * @param id 书目 ID
     * @return 队列视图
     */
    @GetMapping("/{id}/queue")
    @PreAuthorize("hasAuthority('catalog:read')")
    public BaseResponse<BookDtos.QueueView> queue(@PathVariable Long id) {
        return ResultUtils.success(bookQueryService.queue(id));
    }
}
