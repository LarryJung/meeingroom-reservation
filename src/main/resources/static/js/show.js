/**
 * Created by larry on 02.08.18.
 */

// VARIABLES =============================================================

// FUNCTIONS =============================================================
function parseDay(day) {
    var dd = day.getDate();
    var mm = day.getMonth() + 1;
    var yyyy = day.getFullYear();
    if (dd < 10) {dd = '0' + dd;}
    if (mm < 10) {mm = '0' + mm;}
    return yyyy + '-' + mm + '-' + dd;
}

function getToday() {
    var day = new Date();
    return parseDay(day);
}

function getPreviousDay(dateObj) {
    dateObj.setDate(dateObj.getDate()-1);
    console.log("what " + dateObj);
    return parseDay(dateObj);
}

function getNextDay(dateObj) {
    dateObj.setDate(dateObj.getDate()+1);
    return parseDay(dateObj);
}

function moveDay(fun) {
    var pageDate = new Date($('#day').html());
    var moveDay = fun(pageDate);
    var roomId = $('#room-id').html();
    var url = '/reservations/' + moveDay + '/rooms/' + roomId;
    console.log(url);
    redirect(url);
}

function myCreateFunction(data) {
    var source = $("#entry-template").html();
    var template = Handlebars.compile(source);
    var html = template([data]);
    console.log(html);
    $("#reservation-table tbody").last().append(html);
}

function redirect(url) {
    window.location.replace(url);
}

function showReservations() {
    var source = $("#entry-template").html();
    var template = Handlebars.compile(source);
    var date = $("#day").text().trim();
    var roomId = $("#room-id").text().trim();
    var url = "/api/reservations/" + date + "/rooms/" + roomId;
    console.log("url : " + url);
    $.ajax({
        url: url,
        dataType: 'json'
    }).then(function (data) {
        console.log(data);
        var html = template(data);
        console.log(html);
        $('#reservation-table tbody').append(html);
    });
}

// EVENT LISTENERS =========================================================
$(document).ready(showReservations());

$(document).on("click", "#btnCancel", function (e) {
    e.preventDefault();
    var select = $(this);
    var currentUrl = window.location.href;
    var relativeUrl = currentUrl.replace("http://localhost:8080", "");
    console.log("reservation id : " + select.attr('value'));
    var cancelUrl = "/api" + relativeUrl + '?' + $.param({"reservationId": select.attr('value'), "bookerId": select.attr('content')});
    console.log(cancelUrl);
    var token = localStorage.getItem('token');
    $.ajaxSetup({
        headers: {'Authorization': 'Bearer ' + token}
    });
    $.ajax({
        url: cancelUrl,
        type: 'DELETE',
        success: function (data) {
            console.log(data);
            select.closest('tr').remove();
        },
        error: function (data) {
            alert("예약 취소 에러!")
        }
    });
});

$(document).on("click", "#btnSave", function (e) {
    e.preventDefault();
    var reservation = {
        reservedDate: $('#reserved-date').val(),
        startTime: $('#start-time').val(),
        endTime: $('#end-time').val(),
        numberOfAttendee: $('#number-of-attendee').val(),
    };
    var token = localStorage.getItem('token');
    //Setup headers here and than call ajax
    $.ajaxSetup({
        headers: {'Authorization': 'Bearer ' + token}
    });
    console.log(reservation);
    $.ajax({
        url: $("#reservation-form").attr("action"),
        type: 'POST',
        data: JSON.stringify(reservation),
        dataType: 'json',
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            console.log(data);
            if(getToday() == data.reservedDate) {
                myCreateFunction(data);
            }
        },
        error: function (data) {
            alert("예약 등 에러!")
        }
    });
});

$(document).on("click", "#previousDay", function (e) {
    console.log(this);
    e.preventDefault();
    moveDay(getPreviousDay);
});

$(document).on("click", "#nextDay", function (e) {
    console.log(this)
    e.preventDefault();
    moveDay(getNextDay);
});
