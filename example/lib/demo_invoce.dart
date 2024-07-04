import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class DemoInvoice extends StatelessWidget {
  const DemoInvoice({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(10),
      // margin: const EdgeInsets.fromLTRB(30, 10, 10, 10),
      decoration: BoxDecoration(
          color: Colors.white, border: Border.all(color: Colors.black)),
      child: const Column(
        children: [
          Text('Gấu Store - Tiệm bánh đến từ xứ sở mặt trời mọc'),
          Text('0999.484.733'),
          Text('Địa chỉ: 123 Nguyễn Văn Linh, Đà Nẵng'),
          Text('----------------------------'),
          Text('Hóa đơn bán hàng'),
          Text('Mã hóa đơn: 123456789'),
          Text('Ngày: 01/01/2021'),
          Text('----------------------------'),
          Row(
            children: [
              Expanded(flex: 5, child: Text('Tên sản phẩm')),
              Expanded(flex: 2, child: Text('Số lượng')),
              Expanded(flex: 3, child: Text('Giá', textAlign: TextAlign.right)),
              SizedBox(width: 10)
            ],
          ),
          Row(
            children: [
              Expanded(flex: 5, child: Text('Bánh mỳ')),
              Expanded(flex: 2, child: Text('1')),
              Expanded(
                  flex: 3, child: Text('30.000đ', textAlign: TextAlign.right)),
              SizedBox(width: 10)
            ],
          ),
          Row(
            children: [
              Expanded(flex: 5, child: Text('Bánh kem')),
              Expanded(flex: 2, child: Text('2')),
              Expanded(
                  flex: 3, child: Text('50.000đ', textAlign: TextAlign.right)),
              SizedBox(width: 10)
            ],
          ),
          Row(
            children: [
              Expanded(flex: 5, child: Text('Bánh trái cây')),
              Expanded(flex: 2, child: Text('1')),
              Expanded(
                  flex: 3, child: Text('100.000đ', textAlign: TextAlign.right)),
              SizedBox(width: 10)
            ],
          ),
          Row(
            children: [
              Expanded(flex: 5, child: Text('Bánh gato')),
              Expanded(flex: 2, child: Text('1')),
              Expanded(
                  flex: 3, child: Text('50.000đ', textAlign: TextAlign.right)),
              SizedBox(width: 10)
            ],
          ),
          Text('----------------------------'),
          Row(
            children: [
              Text('Tổng cộng:'),
              Spacer(),
              Text(' 230.000đ'),
              SizedBox(width: 10)
            ],
          ),
          Text('----------------------------'),
          Text('Cảm ơn quý khách!'),
          Text('Hẹn gặp lại!'),
          // Custom image
          FlutterLogo(size: 50)
        ],
      ),
    );
  }
}
