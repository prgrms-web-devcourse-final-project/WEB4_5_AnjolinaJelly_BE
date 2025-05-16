package com.jelly.zzirit.domain.order.service.order;

import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QueryOrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    QueryOrderService orderService;

}
