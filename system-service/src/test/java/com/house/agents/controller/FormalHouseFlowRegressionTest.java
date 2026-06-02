package com.house.agents.controller;

import com.house.agents.entity.House;
import com.house.agents.entity.SysRole;
import com.house.agents.entity.SysUser;
import com.house.agents.result.R;
import com.house.agents.service.HouseService;
import com.house.agents.service.OssService;
import com.house.agents.service.ShareEntityService;
import com.house.agents.service.SubwayService;
import com.house.agents.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FormalHouseFlowRegressionTest {

    private HouseController houseController;
    private OssController ossController;
    private HouseService houseService;
    private OssService ossService;

    @BeforeEach
    public void setUp() {
        houseController = new HouseController();
        ossController = new OssController();

        houseService = Mockito.mock(HouseService.class);
        ossService = Mockito.mock(OssService.class);
        RedisTemplate redisTemplate = Mockito.mock(RedisTemplate.class);
        BoundValueOperations boundValueOperations = Mockito.mock(BoundValueOperations.class);

        when(redisTemplate.boundValueOps(any())).thenReturn(boundValueOperations);
        when(boundValueOperations.get()).thenReturn(buildAdminUser());

        org.springframework.test.util.ReflectionTestUtils.setField(houseController, "houseService", houseService);
        org.springframework.test.util.ReflectionTestUtils.setField(houseController, "redisTemplate", redisTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(houseController, "subwayService", Mockito.mock(SubwayService.class));
        org.springframework.test.util.ReflectionTestUtils.setField(houseController, "sysUserService", Mockito.mock(SysUserService.class));
        org.springframework.test.util.ReflectionTestUtils.setField(houseController, "shareEntityService", Mockito.mock(ShareEntityService.class));
        org.springframework.test.util.ReflectionTestUtils.setField(ossController, "ossService", ossService);
    }

    @Test
    public void saveAndUpdateShouldStillDelegateToFormalHouseService() {
        House house = new House();
        house.setCommunity("测试小区");
        house.setRoomNumber("1室1厅");
        house.setRent(new BigDecimal("4200"));
        house.setUserId(7001L);

        R saveResult = houseController.save(house, "admin-token");
        assertEquals(200, saveResult.getCode().intValue());
        assertEquals(7001L, house.getUserId());
        verify(houseService).save(house);

        R updateResult = houseController.updateById(house, "admin-token");
        assertEquals(200, updateResult.getCode().intValue());
        verify(houseService).updateById(house);
    }

    @Test
    public void exportAndImportShouldStillDelegateToFormalExcelService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "houses.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake".getBytes()
        );

        R importResult = houseController.importHouse(request, file, "admin-token");
        assertEquals(200, importResult.getCode().intValue());
        verify(houseService).importHouses(eq(file), eq(7001L));

        houseController.download(request, response, "admin-token");
        verify(houseService).exportHouses(eq(response), eq(7001L));
    }

    @Test
    public void ossUploadShouldStillRequireHouseIdAndDelegateToFormalOssService() {
        MockMultipartFile file = new MockMultipartFile("file", "room.jpg", "image/jpeg", "fake-image".getBytes());
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(ossService.upload(eq(file), eq("1001"), eq(request), eq(response))).thenReturn("https://oss/1001/room.jpg");

        R result = ossController.upload(file, "1001", request, response);
        assertEquals(200, result.getCode().intValue());
        verify(ossService).upload(eq(file), eq("1001"), eq(request), eq(response));
    }

    private SysUser buildAdminUser() {
        SysUser user = new SysUser();
        user.setId(7001L);
        user.setUsername("admin");
        user.setName("管理员");
        SysRole role = new SysRole();
        role.setRoleCode("SYSTEM");
        user.setRoleList(List.of(role));
        return user;
    }
}
