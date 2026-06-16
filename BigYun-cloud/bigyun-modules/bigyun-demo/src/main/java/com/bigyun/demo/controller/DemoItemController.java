package com.bigyun.demo.controller;

import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.core.web.page.TableDataInfo;
import com.bigyun.common.log.annotation.Log;
import com.bigyun.common.log.enums.BusinessType;
import com.bigyun.common.security.annotation.RequiresPermissions;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.demo.domain.DemoItem;
import com.bigyun.demo.service.IDemoItemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 社区示例模块接口。
 *
 * @author bigyun
 */
@RestController
@RequestMapping("/item")
public class DemoItemController extends BaseController
{
    private final IDemoItemService demoItemService;

    public DemoItemController(IDemoItemService demoItemService)
    {
        this.demoItemService = demoItemService;
    }

    @RequiresPermissions("demo:item:list")
    @GetMapping("/list")
    public TableDataInfo list(DemoItem demoItem)
    {
        startPage();
        List<DemoItem> list = demoItemService.selectDemoItemList(demoItem);
        return getDataTable(list);
    }

    @RequiresPermissions("demo:item:query")
    @GetMapping("/{itemId}")
    public AjaxResult getInfo(@PathVariable Long itemId)
    {
        return success(demoItemService.selectDemoItemById(itemId));
    }

    @RequiresPermissions("demo:item:add")
    @Log(title = "示例数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody DemoItem demoItem)
    {
        demoItem.setCreateBy(SecurityUtils.getUsername());
        return toAjax(demoItemService.insertDemoItem(demoItem));
    }

    @RequiresPermissions("demo:item:edit")
    @Log(title = "示例数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody DemoItem demoItem)
    {
        demoItem.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(demoItemService.updateDemoItem(demoItem));
    }

    @RequiresPermissions("demo:item:remove")
    @Log(title = "示例数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(demoItemService.deleteDemoItemByIds(itemIds));
    }
}

