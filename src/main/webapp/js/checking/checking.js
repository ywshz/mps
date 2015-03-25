(function($) {
	
	var $loading = $('.loading'),
		$slt = $('#slt'),
		$aPd = $('#aPd'),
		$aFm = $('#aFm'),
		$aDiff = $('#aDiff'),
		$standalone = $('#standalone');
	
	var lastSltVal,
		selected;
	
	var checking = {
		init : function() {
			this.addEventListener();
			ajaxQueryOrderChecked();
		},
		
		addEventListener: function() {
			$slt.change(function(evt) {
				var val = $(this).val();
				var json = $(this).find('option:selected').data('json');
				if (val && lastSltVal != val) {
					$('#title').text(json.title);
					$('#diffOrderCount').text(json.diffOrderCount);
					$('#diffAmount').text(json.diffAmount);
					$('#pdExcludesSize').text(json.pdExcludes.length);
					$('#fmExcludesSize').text(json.fmExcludes.length);
					$('#diffOrdersSize').text(json.diffOrders.length);
					$('#diffOrderRatio').text(parseFloat(json.diffOrderRatio).toFixed(3));
					$standalone.empty();
				}
				lastSltVal = val;
				selected = json;
			});
			$aPd.click(function() { checking.renderStandalone(0); });
			$aFm.click(function() { checking.renderStandalone(1); });
			$aDiff.click(function() { checking.renderStandalone(2); });
		},
		
		render: function(json) {
			$(json).each(function(i, item) {
				var id = item.id;
				var title = item.title;
				var $option = $('<option>');
				$slt.append($option.val(id).text(title).data('json', item));
			});
		},
		
		renderStandalone: function(type) {
			if (!selected) {
				return;
			}
			var excludes = null;
			if (type == 0) {
				excludes = selected.pdExcludes;
			}
			else if (type == 1) {
				excludes = selected.fmExcludes;
			}
			else if (type == 2) {
				excludes = selected.diffOrders;
			}
			if (null != excludes) {
				$standalone.empty();
				var tbl = $('<table>').addClass('table table-bordered');
				var thead = $('<thead>');
				var tbody = $('<tbody>');
				var thead_tr = $('<tr>');
				thead_tr.append('<th>订单号</th>');
				thead_tr.append('<th>手机号码</th>');
				thead_tr.append('<th>支付编号</th>');
				thead_tr.append('<th>支付金额</th>');
				thead_tr.append('<th>支付状态</th>');
				thead_tr.append('<th>支付时间</th>');
				tbl.append(thead.append(thead_tr)).append(tbody);
				$(excludes).each(function(i, item) {
					if (checking.isArray(item)) {
						var tr = $('<tr>');
						var orderId = $('<td>').html('<div>' + item[0].orderId + '</div>');
						var mobileNum = $('<td>').html('<div>' + item[0].mobileNum + '</div>'+'<div>' + item[1].mobileNum + '</div>');
						if (item[0].mobileNum != item[1].mobileNum) mobileNum.addClass('text-danger');
						var payId = $('<td>').html('<div>' + item[0].payId + '</div>'+'<div>' + item[1].payId + '</div>');
						if (item[0].payId != item[1].payId) payId.addClass('text-danger');
						var payCount = $('<td>').html('<div>' + item[0].payCount + '</div>'+'<div>' + item[1].payCount + '</div>');
						if (item[0].payCount != item[1].payCount) payCount.addClass('text-danger');
						var payStatus = $('<td>').html('<div>' + item[0].payStatus + '</div>'+'<div>' + item[1].payStatus + '</div>');
						if (item[0].payStatus != item[1].payStatus) payStatus.addClass('text-danger');
						
						var date = new Date(); date.setTime(parseInt(item[0].payTime));
						var date1 = new Date(); date1.setTime(parseInt(item[1].payTime));
						var payTime = $('<td>').html('<div>' + date.Format('yyyy-MM-dd hh:mm:ss') + '</div>'+'<div>' + date1.Format('yyyy-MM-dd hh:mm:ss') + '</div>');
						tr.append(orderId).append(mobileNum).append(payId).append(payCount).append(payStatus).append(payTime);
						tbody.append(tr);
					}
					else {
						var tr = $('<tr>');
						var orderId = $('<td>').text(item.orderId);
						var mobileNum = $('<td>').text(item.mobileNum);
						var payId = $('<td>').text(item.payId);
						var payCount = $('<td>').text(item.payCount);
						var payStatus = $('<td>').text(item.payStatus);
						var date = new Date();
						date.setTime(parseInt(item.payTime));
						var payTime = $('<td>').text(date.Format('yyyy-MM-dd hh:mm:ss'));
						tbody.append(tr.append(orderId).append(mobileNum).append(payId).append(payCount).append(payStatus).append(payTime));
					}
				});
				$standalone.append(tbl);
			}
		},
		
		isArray : function(v){
            return toString.apply(v) === '[object Array]';
        }
	};
	
	function ajaxQueryOrderChecked() {
		$.getJSON(window.base + '/checking/queryOrderChecked.do', function(json) {
			$loading.addClass('hide');
			checking.render(json);
		});
	}
	
	$(function() {
		checking.init();
	});
	
})(jQuery);
Date.prototype.Format = function(fmt) {
	var o = {
		"M+" : this.getMonth() + 1, // 月份
		"d+" : this.getDate(), // 日
		"h+" : this.getHours(), // 小时
		"m+" : this.getMinutes(), // 分
		"s+" : this.getSeconds(), // 秒
		"q+" : Math.floor((this.getMonth() + 3) / 3), // 季度
		"S" : this.getMilliseconds()
	};
	if (/(y+)/.test(fmt))
		fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	for ( var k in o)
		if (new RegExp("(" + k + ")").test(fmt))
			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
	return fmt;
};