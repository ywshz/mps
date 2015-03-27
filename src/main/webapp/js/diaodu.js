/**
 * Created by wangshu.yang on 2015/3/26.
 */
$(document).ready(main());
var editor
function main(){
    initEditor();
}

function initEditor(){
    editor = CodeMirror.fromTextArea(document.getElementById("edit-script"), {
        lineNumbers : true,
        mode : 'text/x-hive',
        indentWithTabs : true,
        smartIndent : true,
        matchBrackets : true,
        autofocus : true,
        width: '100%',
        height: '400px'
    });

}

function enter(parent){
    $("#parent-input").val(parent);
    $("#refresh-form").submit();
}

function detail(name){
    $.post(BASE_PATH + "/files/detail.do",{jobName:name, parent:$("#parent-input").val()},function(data){
        $('#detail-modal').modal({
            keyboard: false,
            backdrop: 'static',
            show : true
        });


        $("#inputID").val(data.id);
        $("#inputName").val(data.name);
        $("#inputScheduleType").val(data.jobType);

        if (data.scheduleType == "CRON") {
            $("#radioSchedualByTime").prop("checked",true);
            $("#radioSchedualByDependency").prop("checked",false);
            $("#inputCron").val(data.cron);

            $("#dependency").attr("readonly","");
            $("#real-dependency").attr("readonly","");
            $("#inputCron").removeAttr("readonly");
            $("#dependency").val("");
            $("#real-dependency").val("");
        } else {
            $("#radioSchedualByTime").prop("checked",false);
            $("#radioSchedualByDependency").prop("checked",true);
            $("#dependency").val(data.dependency);
            $("#real-dependency").val(data.realDependency);

            $("#dependency").removeAttr("readonly");
            $("#real-dependency").removeAttr("readonly");
            $("#inputCron").attr("readonly","");
            $("#inputCron").val("");
        }
        editor.setValue(data.script==null?"":data.script);

        //从隐藏层显示,需要延迟刷新,否则仍无法正常显示
        setTimeout(function(){
            editor.refresh();
        },200);


    });

}