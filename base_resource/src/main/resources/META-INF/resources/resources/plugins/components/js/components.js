/**
 * Created by pgwt on 2016/3/2 0002.
 */
//选择数量组建 有个三个可选部分，1 加号 2减号 3.输入框
//加号减号分别有可选状态和不可选状态，代表着组建value的上下限
//输入框有两种状态 1.可以输入 2.不可以输入 同样也共有上限下限
//加减号的组件一版作为表单的一项 还有就是加号减号都有一些毁掉函数
//js组件 通过js调用 确定组件再页面中的位置  需要父节点
//使用例子：<div class="addsub" data-option='{"upLimit":"${seckillProduct.limitAmount}","inputName":"amount"}'></div>
//需要引用 components.css文件
var ChooseCountComponent = function () {
    var parentDiv = $(".addsub");
    var options = new Object();
    options["upLimit"] = 100; //默认上限  当数值达到上限的时候 加号不可点
    options["downLimit"] = 1; //默认下限   当数值达到下限的时候 减号不可点
    options["inputStatus"] = 0; //0为不可输入  1为可输入
    options["defaultValue"] = 1;
    options["inputName"] = "";
    var componentOption = parentDiv.attr("data-option"); //json字符串
    if (typeof componentOption != "undefined") {
        componentOption = JSON.parse(componentOption);

        if (typeof componentOption["defaultValue"] == "string") {
            options["defaultValue"] = Number(componentOption["defaultValue"]);
        }
        if (typeof componentOption["upLimit"] == "string") {
            options["upLimit"] = Number(componentOption["upLimit"]);
        }
        if (typeof componentOption["downLimit"] == "string") {
            options["downLimit"] = Number(componentOption["downLimit"]);
        }
        if (typeof componentOption["inputStatus"] == "string") {
            options["inputStatus"] = Number(componentOption["inputStatus"]);
        }
        if (typeof componentOption["inputName"] == "string") {
            options["inputName"] = componentOption["inputName"];
        }
    }
    var disabled = "";
    if (options["inputStatus"] == 1) {
        disabled = "disabled";
    }
    var html = '<div class="opt"><a class="sub" title="减" ></a><input name="' + options["inputName"] + '" ' + disabled + ' class="ipt-txt" type="text" value="' + options["defaultValue"] + '" /><a class="add" title="加" ></a></div>'
    //加号的事件
    var addAction = function (e) {
        var inputElement = $(e.target).parent().find(".ipt-txt");
        var actionOptions = options;
        var currentValue = Number(inputElement.val());
        if (Number(currentValue) < actionOptions["upLimit"]) {
            currentValue += 1;
            inputElement.val(currentValue);
        }
    }
    var subAction = function (e) {
        var inputElement = $(e.target).parent().find(".ipt-txt");
        var actionOptions = options;
        var currentValue = Number(inputElement.val());
        if (Number(currentValue) > actionOptions["downLimit"]) {
            currentValue -= 1;
            inputElement.val(currentValue);
        }
    }
    parentDiv.on("click", ".add", addAction);
    parentDiv.on("click", ".sub", subAction);
    parentDiv.html(html);
}

//联动选择组件 例如先选择省再选城市再选择区域
//组件之中的元素可分为  item1 item2 item3 .... itemN
//考虑组件之中数据的来源来源，一般情况下是通过ajax从服务器中动态获取
//在等级i的元素在选中某个元素的时候等级i+1的元素就会根据等级i选择的元素列出数据
//每个元素都是一个select 所有需要判断当前组建是否有下一等级的组件，如果有就需要绑定onChange事件，如果没有就不用绑定了
//根据配置给的联动组建的大小来创建item对象item对象当中需要包含数据来源（这里的数据来源默认是ajax请求过来的，所以需要把每个层级的url传递进来)
/**
 <div class="selectGroup">
 <div class="selectItem" dataFrom="<c:url value="/banner/platform.do"/>" initValue="选择平台"></div>
 <div class="selectItem" id="groupName" name="groupName" paramName="platform"dataFrom="<c:url value="/banner/position.do"/>" initValue="请选位置"></div>
 </div>
 */
//联动组件中的元素个数由 selectItem决定，在div中有几个selectItem就是有几个元素
//使用localStorage ，就将取过来的数据存储到客户端当作是客户端的缓存，例如城市的数据，主要是第二级和第三级。。的，
//如果之前选过河北省 那么代码会先查看localStroage中是否有相应的数据（河北省id对应的城市数据列表），如果有就直接用，如果没有再通过数据来源从服务器获取
//取过来之后，会将城市的数据存入localStorage当中，下次再选择河北省的时候就不用再去取数据了，这样页面的响应速度会有所提升
//需要判断当前浏览器是否支持localStorage
//这里需要区分横竖两种样式，暂时先考虑移动端竖着的样式
//添加class参数用来传递select以及option的样式
var SelectGroupComponent = function () {
    //组件元素列表
    //联动的层级数量是通过配置文件配置而来的
    //声明一个select对象

    var select = function (clazz, name, dataFrom, id, initValue, grade, paramName) {
        this.clazz = clazz;
        this.name = name;
        this.dataFrom = dataFrom;
        this.id = id;
        this.initValue = initValue;
        this.grade = grade;
        this.html = html;
        this.paramName = paramName;
    };

    var selectMap = new Object(); //用来按顺序存放所有的select标签
    var parentDiv = $(".selectGroup");
    var selectItems = parentDiv.find(".selectItem");
    var groupSize = selectItems.length;
    //第一步 绘制下拉菜单
    for (var i = 0; i < groupSize; i++) {
        var selectItem = selectItems[i];
        selectItem = $(selectItem);
        console.log(selectItem);
        var selectTemp = new select(selectItem.attr("class"), selectItem.attr("name"), selectItem.attr("dataFrom"), selectItem.attr("id"), selectItem.attr("initValue"), i, selectItem.attr("paramName"));
        var html = "";
        if (i >= 0 && i < (groupSize - 1)) {
            html += '<select isChange="true"';
        } else {
            html += '<select ';
        }
        html += ' paramName="' + selectTemp.paramName + '" dataFrom="' + selectTemp.dataFrom + '" grade="' + selectTemp.grade + '" class="' + selectTemp.clazz + '" id="' + selectTemp.id + '" name="' + selectTemp.name + '"><option>' + selectTemp.initValue + '</option></select> '
        selectTemp.html = html;
        selectMap[i] = selectTemp;
    }
    //获得第一级的数据
    var first = selectMap["0"];
    ajaxRequest(first.dataFrom, {}, function (data) {
        var out = '<option>' + first.initValue + '</option>';
        for (var i = 0; i < data.length; i++) {
            out += '<option value="' + data[i][0] + '">' + data[i][1] + '</option>';
        }
        first.html = '<select isChange="true" paramName="' + first.paramName + '" dataFrom="' + first.dataFrom + '" grade="' + first.grade + '" class="' + first.clazz + '" id="' + first.id + '" name="' + first.name + '">' + out + '</select>';
        selectMap["0"] = first;
        var result = "";
        for (var key in selectMap) {
            result += selectMap[key].html;
        }
        parentDiv.html(result);
    });
    //选择当前级别的select之后出发下一级的数据获取，数据获取是通过当前级别所选戳来的选项去获取下一级数据
    var onChangeAction = function (e) {
        //可以得到当前级别选中的值（value）
        //问题如何得到下一级别的select .next("select")
        //选择的时候所以的低级节点都需要重置
        var $currentElement = $(e.target);
        var nextSelect = $currentElement.next();
        var nextSelectNeededParamName = nextSelect.attr("paramName");
        var nextSelectDataFrom = nextSelect.attr("dataFrom");
        //@TODO 待验证
        var parmObject = new Object();
        parmObject[nextSelectNeededParamName] = $currentElement.val();
        ajaxRequest(nextSelectDataFrom, parmObject, function (data) {
            var out = "";
            for (var i = 0; i < data.length; i++) {
                out += '<option value="' + data[i][0] + '">' + data[i][1] + '</option>'
            }
            //@TODO 待优化
            var nextFirstOption = nextSelect.children()[0];
            console.log(nextFirstOption);
            out = nextFirstOption.outerHTML + out;
            nextSelect.html(out);
            var nextNextSelect = nextSelect.next();
            var nextNextFirstOption = nextNextSelect.children()[0];
            nextNextSelect.html(nextNextFirstOption.outerHTML);
            nextNextSelect.child("option:first").prop("selected", 'selected')
        });
    };
    parentDiv.on("change", "[isChange=true]", onChangeAction);
};
