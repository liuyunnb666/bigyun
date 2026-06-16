package com.bigyun.demo.service.impl;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.demo.domain.DemoItem;
import com.bigyun.demo.mapper.DemoItemMapper;
import com.bigyun.demo.service.IDemoItemService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 社区示例服务实现。
 *
 * @author bigyun
 */
@Service
public class DemoItemServiceImpl implements IDemoItemService
{
    private final DemoItemMapper demoItemMapper;

    public DemoItemServiceImpl(DemoItemMapper demoItemMapper)
    {
        this.demoItemMapper = demoItemMapper;
    }

    @Override
    public List<DemoItem> selectDemoItemList(DemoItem demoItem)
    {
        return demoItemMapper.selectDemoItemList(demoItem);
    }

    @Override
    public DemoItem selectDemoItemById(Long itemId)
    {
        return demoItemMapper.selectDemoItemById(itemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDemoItem(DemoItem demoItem)
    {
        checkItemCodeUnique(demoItem);
        if (StringUtils.isEmpty(demoItem.getStatus()))
        {
            demoItem.setStatus("0");
        }
        return demoItemMapper.insertDemoItem(demoItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDemoItem(DemoItem demoItem)
    {
        checkItemCodeUnique(demoItem);
        return demoItemMapper.updateDemoItem(demoItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDemoItemByIds(Long[] itemIds)
    {
        return demoItemMapper.deleteDemoItemByIds(itemIds);
    }

    private void checkItemCodeUnique(DemoItem demoItem)
    {
        DemoItem exists = demoItemMapper.selectDemoItemByCode(demoItem.getItemCode());
        if (exists != null && !exists.getItemId().equals(demoItem.getItemId()))
        {
            throw new ServiceException("示例编码已存在");
        }
    }
}

