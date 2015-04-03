/**
 * Created by wangshu.yang on 2015/3/26.
 */
$(document).ready(main());
var editor
function main() {
    initButtons();
    initEditor();
}

function initButtons() {
    $("#add-task-btn").click(function(){
        viewAddTask();
    });

    $("#add-group-btn").click(function(){
        viewAddGroup();
    });

    $("#save-group-btn").click(function(){
        saveGroup();
    });

    $("#save-task-btn").click(function(){
        saveOrUpdateTask();
    });

}


function initEditor() {
    editor = CodeMirror.fromTextArea(document.getElementById("edit-script"), {
        lineNumbers: true,
        mode: 'text/x-hive',
        indentWithTabs: true,
        smartIndent: true,
        matchBrackets: true,
        autofocus: true,
        width: '100%',
        height: '400px'
    });

}

function viewAddTask() {
    $('#detail-modal').modal({
        keyboard: false,
        backdrop: 'static',
        show: true
    });

    $("#inputID").val("");
    $("#inputName").val("");
    $("#inputName").removeAttr("readonly");
    $("#inputScheduleType").val("SHELL");

    $("#radioSchedualByTime").prop("checked", true);
    $("#radioSchedualByDependency").prop("checked", false);
    $("#inputCron").val("0 0 0 * * * ?");

    $("#dependency").val("");
    $("#real-dependency").val("");
    editor.setValue("script here");

    //从隐藏层显示,需要延迟刷新,否则仍无法正常显示
    setTimeout(function () {
        editor.refresh();
    }, 200);
}

function viewAddGroup(){
    $('#add-group-modal').modal({
        keyboard: false,
        backdrop: 'static',
        show: true
    });
}

function saveOrUpdateTask(){
    var name= $("#inputName").val();
    var jobType= $("#inputJobType").val();
    var scheduleType;
    var cron,dependency;
    if(document.getElementById('radioSchedualByTime').checked){
        //cron,dependency
        scheduleType= 'CRON';
        cron = $("#inputCron").val();
    }else{
        scheduleType= 'DEPENDENCY';
        dependency = $("#dependency").val();
    }
    var script= editor.getValue();

    if($("#inputID").val() == "" ){
        $.post(BASE_PATH+"/files/save-update-task.do",{parent: $("#parent-input").val() ,name:name,jobType:jobType,scheduleType:scheduleType,cron:cron,dependency:dependency,script:script},function(data){
            if(data.code==200) {
                alert("修改成功");
                $("#refresh-form").submit();
            } else{
                alert("修改失败,请重试!");
            }
        });
    }else{

        $.post(BASE_PATH+"/files/save-update-task.do",{id:$("#inputID").val()  ,parent: $("#parent-input").val() ,name:name ,jobType:jobType,scheduleType:scheduleType,cron:cron,dependency:dependency,script:script},function(data){
            if(data.code==200) {
                alert("修改成功");
                $("#refresh-form").submit();
            } else{
                alert("修改失败,请重试!");
            }
        });
    }


}

function saveGroup(){
    if($("#inputGroupName").val().trim() == '' ){
        alert("名字不能为空");
    }else{
        $("#add-group-form").submit();
    }
}

function enter(parent) {
    $("#parent-input").val(parent);
    $("#refresh-form").submit();
}

function detail(name) {
    $.post(BASE_PATH + "/files/detail.do", {jobName: name, parent: $("#parent-input").val()}, function (data) {
        $('#detail-modal').modal({
            keyboard: false,
            backdrop: 'static',
            show: true
        });


        $("#inputID").val(data.id);
        $("#inputName").val(data.name);
        $("#inputName").attr("readonly","");
        //$("#inputScheduleType").val(data.jobType);
        $("#inputJobType option[value="+data.jobType+"]").attr("selected", true);
        if (data.scheduleType == "CRON") {
            $("#radioSchedualByTime").prop("checked", true);
            $("#radioSchedualByDependency").prop("checked", false);
            $("#inputCron").val(data.cron);

            $("#dependency").val("");
            $("#real-dependency").val("");
        } else {
            $("#radioSchedualByTime").prop("checked", false);
            $("#radioSchedualByDependency").prop("checked", true);
            $("#dependency").val(data.dependency);
            $("#real-dependency").val(data.realDependency);

            $("#inputCron").val("");
        }
        editor.setValue(data.script == null ? "" : data.script);

        //从隐藏层显示,需要延迟刷新,否则仍无法正常显示
        setTimeout(function () {
            editor.refresh();
        }, 200);
    });
}

function deleteTask(name){
    $.post(BASE_PATH+"/files/delete-task.do",{parent: $("#parent-input").val() ,name: name},function(data){
        if(data.code==200) {
            alert("操作成功");
            $("#refresh-form").submit();
        } else{
            alert("操作失败,请重试!");
        }
    });
}

function startOrStop(){
    $.post(BASE_PATH+"/files/start-or-stop.do",{parent: $("#parent-input").val() ,name: name},function(data){
        if(data.code==200) {
            alert("操作成功");
            $("#refresh-form").submit();
        } else{
            alert("操作失败,请重试!");
        }
    });
}