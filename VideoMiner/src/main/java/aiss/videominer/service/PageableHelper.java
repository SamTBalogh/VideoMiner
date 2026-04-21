package aiss.videominer.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageableHelper {

    static Pageable build(int page, int size, String order) {
        if (order != null) {
            if (order.startsWith("-")) {
                return PageRequest.of(page, size, Sort.by(order.substring(1)).descending());
            }
            return PageRequest.of(page, size, Sort.by(order).ascending());
        }
        return PageRequest.of(page, size);
    }
}
