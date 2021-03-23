/* 
    This script file contains some general functions that can be used universally across the bot.
*/

//Some default moment calendar formats
var pod = context.entities.PhaseOfDay ? " " + context.entities.PhaseOfDay.split("-")[2] : "";

var calendarFormatList = {
    sameDay: '[Today] h:mm a',
    nextDay: '[Tomorrow] h:mm a',
    nextWeek: 'ddd h:mm a',
    lastDay: '[Yesterday] h:mm a',
    lastWeek: '[Last] dddd h:mm a',
    sameElse: 'ddd, MMM Do, h:mm a'
};

var calendarFormat = {
    sameDay: '[today] h:mm a',
    nextDay: '[tomorrow] h:mm a',
    nextWeek: 'dddd h:mm a',
    lastDay: '[yesterday] h:mm a',
    lastWeek: '[last] dddd h:mm a',
    sameElse: 'dddd, MMMM Do, h:mm a'
};

var calendarFormatDateOnly = {
    sameDay: '[today]' + (pod ? (" [" + pod + "]") : ""),
    nextDay: '[tomorrow]' + (pod ? (" [" + pod + "]") : ""),
    nextWeek: '[on] dddd' + (pod ? (" [" + pod + "]") : ""),
    lastDay: '[yesterday]' + (pod ? (" [" + pod + "]") : ""),
    lastWeek: '[on last] dddd' + (pod ? (" [" + pod + "]") : ""),
    sameElse: '[on] dddd, MMMM Do ' + (pod ? (" [" + pod + "]") : "")
};

var calendarFormatDateOnlyHeader = {
    sameDay: '[Today]',
    nextDay: '[Tomorrow]',
    nextWeek: 'dddd',
    lastDay: '[yesterday]',
    lastWeek: '[last] dddd',
    sameElse: 'dddd'
};

// Default the time zone for Moment
koreUtil.moment.tz.setDefault(getUserCurrentTimeZone());

/* General Utility Functions */

// Trace progress in context
function debugLog(level, text) {
    "use strict";

    if (!context.debugLog) {
        context.debugLog = [];
    }
    var msg = level.toUpperCase() + ": " + text;
    context.debugLog.push(msg);
}

//String Functions


/* string.toCapitalCase()
  Handles inputs:
    UPPERCASE, "space separated".
  eg: 'STRING TO CONVERT'.toCapitalCase()  returns: 'String to convert'
 */
String.prototype.toCapitalCase = function() {
    return this.charAt(0).toUpperCase() + this.slice(1).toLowerCase();
};


/* string.toTitleCase()
  Handles inputs:
    UPPERCASE, "space separated".
  eg: 'STRING TO CONVERT'.toTitleCase() returns: 'String To Convert'
 */
String.prototype.toTitleCase = function() {
    return this.split(' ')
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(' ');
};

/* string.anyStringToTitleCase()
  Handles inputs:
    "space separated", camelCase, ClassCase, UPPERCASE, snake_case, dash-case, object.case.
  eg: 'string-toConvert_has a.very ComplexStructure'.anyStringToTitleCase()
    returns: 'String To Convert Has A Very Complex Structure'
 */
const STRING_DECAMELIZE_REGEXP = (/([a-z\d])([A-Z])/g);
const STRING_SEPARATORS_REGEXP = (/(\-|\_|\.)/g);

String.prototype.anyStringToTitleCase = function() {
    return this.replace(STRING_DECAMELIZE_REGEXP, '$1 $2')
        .replace(STRING_SEPARATORS_REGEXP, ' ')
        .split(' ')
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(' ');
};

// allow string format
String.prototype.format = function() {
    var formatted = this;
    for (var prop in arguments[0]) {
        var regexp = new RegExp('\\{' + prop + '\\}', 'gi');
        formatted = formatted.replace(regexp, arguments[0][prop]);
    }
    return formatted;
};


// narrator template wrapper if inside try out flow
function narratorWrapper(msgtemplate) {
    if (!context.selectedScript) {
        return msgtemplate;
    }
    if (!BotUserSession.get("errors")) {
        BotUserSession.put("errors", [], 2)
    }
    var current = context.selectedScript.shift();
    while (current.node && current.node !== context.currentNodeName) {
        context.session.BotUserSession.errors.push(["dumping at {currentNode}".format({
            "currentNode": context.currentNodeName
        }), current]);
        var current = context.selectedScript.shift();
    }
    if (context.selectedScript.length > 0) {
        context.nextScope = context.selectedScript[0].scope;
    } else {
        context.nextScope = "END";
    }
    var narratorTemplate = {
        "type": "template",
        "payload": {
            "template_type": "narrator",
            //"speech_hint" : current.speech_hint || current.msg,
            "text": current.msg,
            "composeText": current.composeText,
            "focus": current.focus || "send"
        }
    };
    if (msgtemplate) {
        narratorTemplate.payload.childTemplate = msgtemplate;
    }
    narratorTemplate.payload.is_end = current.isEnd || false;
    return narratorTemplate;
}

// Generate a random number in a range
function rndNum(min, max) {
    "use strict";

    var num = Math.floor(Math.random() * (max - min + 1)) + min;
    return num;
}

//check if trait exists
function traitExists(traitname) {
    return (context.traits !== undefined) && context.traits.includes(traitname);
}

// Return the current personality for the user
function getPersonality() {
    "use strict";

    var typeOfPersonality = context.session.BotUserSession.currentPersonality;
    if (!typeOfPersonality) {
        typeOfPersonality = "neutral";
    }
    return typeOfPersonality;
}


// Return the parameters associated with the last message
function getMessagePayloadData(key) {
    "use strict";

    if (!key) {
        return null;
    }

    var keyParts = key.split(".");
    var obj;

    //if (koreUtil._.has(context.session.BotUserSession, "lastMessage.messagePayload")) {
    var lastMessage = BotUserSession.get("lastMessage")
    if (lastMessage.messagePayload) {
        obj = lastMessage.messagePayload;

        for (var i = 0; i < keyParts.length; i++) {
            var part = keyParts[i];
            if (obj[part]) {
                obj = obj[part];
            } else {
                return null;
            }
        }
    }
    return obj;
}

/* END */



/* Common Message Responses and Templates */

// Return a random message where the keys of an object are of the form "msg1", "msg2" etc
function randomMessageFromObj(obj) {
    "use strict";

    var max = Object.keys(obj).length;
    var num = rndNum(1, max);
    return obj[("msg" + num)];
}


// Return a random message from an array of messages
function randomMessageFromArray(arr) {
    "use strict";

    var max = arr.length;
    var num = rndNum(0, max - 1);
    if (!arr[num]) {
        arr[num] = '';
    } else if (arr[num] === "") {
        arr[num] = '';
    }
    return arr[num];
}


// Return a personality appropriate message
function getPersonalityMessage(responses, personality) {
    "use strict";

    var msg;
    if (!responses) {
        return msg;
    }

    // could just be a string
    if (typeof responses === "string") {
        return responses;
    }

    // might just be a set of messages
    if (responses.msg1) {
        msg = randomMessageFromObj(responses);
        return msg;
    }
    // or an array
    if (Array.isArray(responses)) {
        msg = randomMessageFromArray(responses);
        return msg;
    }

    // use the set appropriate to the personality or an alternative
    while (personality) {
        if (responses[personality]) {
            if (Array.isArray(responses[personality])) {
                msg = randomMessageFromArray(responses[personality]);
            } else {
                msg = randomMessageFromObj(responses[personality]);
            }
            return msg;
        }

        switch (personality) {
            case "casual":
                personality = "neutral";
                break;
            case "neutral":
                personality = "professional";
                break;
            case "professional":
                personality = null;
                break;
        }
    }

    // really shouldn't reach here
    return msg;
}


// Given sets of possible text and voice responses, return a message template with a random message for each
function randomMessageTemplate(textResponses, voiceResponses, quickReplies, addGeneralAck, addIntentAck) {
    "use strict";

    var typeOfPersonality = getPersonality();

    var textmsg = getPersonalityMessage(textResponses, typeOfPersonality);
    var voicemsg = getPersonalityMessage(voiceResponses, typeOfPersonality);

    // might need a common acknowledgement in front of the message
    if (addGeneralAck) {
        var ackmsg;
        if (typeof addGeneralAck === "string") {
            ackmsg = addGeneralAck;
        } else {
            ackmsg = generalAck(typeOfPersonality);
        }
    } else if (addIntentAck) {
        // might need a intent acknowledgement in front of the message
        var ackmsg;
        if (typeof addIntentAck === "string") {
            ackmsg = addIntentAck;
        } else {
            ackmsg = intentAck(typeOfPersonality);
        }
    }
    if (ackmsg) {
        if (textmsg) {
            textmsg = ackmsg + textmsg;
        }
        if (voicemsg) {
            voicemsg = ackmsg + voicemsg;
        }
    }

    var message = {
        "type": "template",
        "payload": {
            "text": textmsg || "",
            "speech_hint": voicemsg || ""
        }
    };

    // might be supplied some quick replies to be spliced in too
    if (Array.isArray(quickReplies)) {
        addQuickReplies(message, "text", quickReplies);
    }

    return message;
}


// Return a random intent acknowledgement
function intentAck(personality) {
    "use strict";

    //This is the list of intent acknowledgement by personality that should only be used right after intent is recognized
    var casualIntentAckList = ["", "😀 OK.", "Got it! 👍 ", "Alright, sure.", "Yup, OK.", "You got it!", "😎 Cool!", "Awesome!", "I can help with that.", "Not a problem."];
    var neutralIntentAckList = ["", "OK.", "Yes, sure.", "I'm on it.", "Got it.", "Sure."];
    var professionalIntentAckList = ["", "Okay.", "Of course.", "I can assist with that.", "Certainly.", "Absolutely.", "Okay, got it!", "Yes, got it."];

    //Switch based on the personality found, default is neutral incase of error or wrong param.
    var num;
    var text;
    switch (personality) {
        case "casual":
            text = randomMessageFromArray(casualIntentAckList);
            break;
        case "neutral":
            text = randomMessageFromArray(neutralIntentAckList);
            break;
        case "professional":
            text = randomMessageFromArray(professionalIntentAckList);
            break;
        default:
            text = "OK.";
    }
    return text + " ";
}


// Return a random general acknowledgement
function generalAck(personality) {
    "use strict";

    //This is the list of general acknowledgement by personality that can be used anywhere in the dialog.
    var casualIntentAckList = ["", "😀 OK.", "Got it! 👍 ", "👍", "OK.", "Alright!", "😎"];
    var neutralIntentAckList = ["", "OK.", "Got it!", "Got that.", "Alright!"];
    var professionalIntentAckList = ["", "Okay.", "Got it.", "Alright."];

    //Switch based on the personality found, default is neutral incase of error or wrong param.
    var num;
    var text;
    switch (personality) {
        case "casual":
            text = randomMessageFromArray(casualIntentAckList);
            break;
        case "neutral":
            text = randomMessageFromArray(neutralIntentAckList);
            break;
        case "professional":
            text = randomMessageFromArray(professionalIntentAckList);
            break;
        default:
            text = "OK.";
    }
    return text + " ";
}


// Add quick replies to a message response
function addQuickReplies(message, contentType, QRTitle, QRPayload) {
    "use strict";

    //If no payload is given, assume title as payload
    QRPayload = QRPayload !== undefined ? QRPayload : QRTitle;

    //Change the message template type to quick replies
    message.payload.template_type = "quick_replies";
    message.payload.quick_replies = [];

    //Loop through and create QR objects and add into the message payload.
    for (var i = 0; i < QRTitle.length; i++) {
        var quickReply = {
            "content_type": contentType,
            "title": QRTitle[i],
            "payload": QRPayload[i]
        };
        message.payload.quick_replies.push(quickReply);
    }
    return message;
}


// Generate a skip button
function skipButton(contentType) {
    "use strict";

    var skipReply = {
        "content_type": contentType,
        "title": "Skip",
        "payload": "Skip"
    };
    return skipReply;
}


// Create buttons from a set of choices 
function generateButtons(choiceList, titleKey, payloadKey) {
    "use strict";

    var buttons = [];

    choiceList.forEach(function(choice) {
        var button = {
            "content_type": "postback",
            "title": choice[titleKey],
            "payload": choice[payloadKey]
        };
        buttons.push(button);
    });

    return buttons;
}

/* END */


/* Slack templates */

//Slack Quick Replies
function addSlackQuickReplies(text, quickReplyArr) {
    "use strict";

    if (!text) {
        text = " ";
    }
    var message = {
        "blocks": [{
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": text
                },
            },
            {
                "type": "divider"
            },
        ]
    };

    var listButtons = quickReplyArr.some(button => button.title.length > 30);

    if (listButtons) {
        for (let i = 0; i < quickReplyArr.length; i++) {
            let choice = {
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": "_" + quickReplyArr[i].title + "_"
                },
                "accessory": {
                    "type": "button",
                    "text": {
                        "type": "plain_text",
                        "emoji": true,
                        "text": "Select"
                    },
                    "value": (quickReplyArr[i].payload || quickReplyArr[i].value || quickReplyArr[i].title)
                }
            }

            message.blocks.push(choice);
        }
        message.blocks.push({
            "type": "divider"
        });

    } else {

        var actions = {
            "type": "actions",
            "elements": []
        }

        for (let i = 0; i < quickReplyArr.length; i++) {
            actions.elements.push({
                'type': "button",
                'text': {
                    'type': "plain_text",
                    'text': quickReplyArr[i].title,
                    'emoji': true,
                },
                'value': (quickReplyArr[i].payload || quickReplyArr[i].value || quickReplyArr[i].title)
            });
        }

        message.blocks.push(actions);
    }

    return message;
}

function addSlackConfirmation(msg) {
    var buttons = [{
            'title': 'Yes',
            'payload': 'yes'
        },
        {
            'title': 'No',
            'payload': 'no'
        }
    ];

    return addSlackQuickReplies(msg, buttons);
}


//Meeting selector button
function slackMeetingSelectTemplate(msg, meetingsObj, displayOnly) {
    "use strict";

    var message = {};
    var attachment = {
        "blocks": [{
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": msg
            }
        }]
    };

    var meetingButton = {};
    var attachments = [];
    meetingsObj.forEach(function(meeting, i) {

        let attendeesArr = [];
        meeting.attendees.forEach(attendee => attendeesArr.push(attendee.name || attendee.email));

        if (attendeesArr.length > 4) {
            let othersLen = attendeesArr.length - 3;
            attendeesArr = attendeesArr.slice(0, 3);
            attendeesArr.push(othersLen + " others");
        }

        let attendeesStr = renderArrays(attendeesArr);

        var details = "*".concat(
            meeting.title,
            "*\n\n*:calendar: \t ",
            koreUtil.moment(meeting.duration.start).calendar(null, calendarFormatList) + " - " + koreUtil.moment(meeting.duration.end).format("h:mm a"),
            "* \n\n *:bust_in_silhouette: \t",
            attendeesStr,
            "*");

        if (meeting.where) {
            details = details.concat("\n\n * :beginner: \t",
                meeting.where,
                "*");
        }

        meetingButton = {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": details
            },
        }
        if (!displayOnly) {
            meetingButton.accessory = {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "emoji": true,
                    "text": "Select"
                },
                "value": meeting.eventId
            }

        }

        attachment.blocks.push(meetingButton);
        attachment.blocks.push(slackDivider());
    });

    attachments.push(attachment);
    message.attachments = attachments;

    return message;

}

function slackMeetingProposalsTemplate(msg, meetingsObj) {
    "use strict";

    var message = {};
    var attachment = {
        "blocks": [{
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": msg
            }
        }]
    };

    var meetingButton = {};
    var attachments = [];
    meetingsObj.forEach(function(meeting, i) {

        let attendeesArr = [];
        meeting.attendees.forEach(attendee => attendeesArr.push(attendee.name || attendee.email));

        if (attendeesArr.length > 4) {
            let othersLen = attendeesArr.length - 3;
            attendeesArr = attendeesArr.slice(0, 3);
            attendeesArr.push(othersLen + " others");
        }

        let attendeesStr = renderArrays(attendeesArr);

        let dateTimeStr = "";
        meeting.slots.forEach(function(slot, i) {

            if (i !== 0) {
                dateTimeStr += "\t\t\t"
            }

            dateTimeStr += "*" + koreUtil.moment(slot.start).calendar(null, calendarFormatList) + " - " + koreUtil.moment(slot.end).format("h:mm a") + "*";

            if (i !== meeting.slots.length - 1) {
                dateTimeStr += "\n";
            }
        })

        meetingButton = {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "*".concat(
                    meeting.meeting_title,
                    "*\n\n:calendar: \t ",
                    dateTimeStr,
                    "\n\n *:bust_in_silhouette: \t",
                    meeting.text,
                    "*")
            },
        }

        attachment.blocks.push(meetingButton);
    });

    attachments.push(attachment);
    message.attachments = attachments;

    return message;

}


//Time Slot Buttons
function generateSlackTimeSlotButtons(text, slots, otherOptions, extendedSlots) {
    "use strict";

    var message = {
        "blocks": [{
            "type": "section",
            "text": {
                "type": "plain_text",
                "text": text,
                "emoji": true
            }
        }]
    };


    var slotButtons = [];
    var button = {};
    slots.forEach(function(slot) {

        let buttonPayload = slot.payload || slot.value;

        button = {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": slot.title,
            },
            "accessory": {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "emoji": true,
                    "text": "Select"
                },
                "value": String(buttonPayload)
            }
        }

        slotButtons.push(button);
    });

    message.blocks = message.blocks.concat(slotButtons);

    if (extendedSlots && extendedSlots.length) {
        var dropdown = {
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "Or select a time from the list:"
            },
            "accessory": {
                "type": "static_select",
                "placeholder": {
                    "type": "plain_text",
                    "text": "Select a time",
                    "emoji": true
                },
                "options": []
            }
        };
        extendedSlots.forEach(function(day) {
            day.slots.forEach(function(slot) {
                let slotTitle = koreUtil.moment(slot.start).calendar(null, calendarFormatList) + " to " + koreUtil.moment(slot.end).format("h:mm a");
                let option = {
                    "text": {
                        "type": "plain_text",
                        "text": slotTitle,
                        "emoji": true
                    },
                    "value": slotTitle
                };
                dropdown.accessory.options.push(option);
            });

        });
        message.blocks.push(dropdown);
    }

    if (otherOptions && otherOptions.length) {

        message.blocks.push(slackDivider());

        var bottomButtons = {
            "type": "actions",
            "elements": []
        };

        otherOptions.forEach(function(option) {
            button = {
                "type": "button",
                "text": {
                    "type": "plain_text",
                    "text": option.title.toCapitalCase(),
                    "emoji": true
                },
                "value": (option.payload || option.value)
            };
            bottomButtons.elements.push(button);

        });
        message.blocks.push(bottomButtons);
    }

    return message;
}

//Final Meeting Confirmation Template

function generateSlackMeetingConfirmation(msg, meetingDetailsObj) {
    "use strict";

    //Process invitees
    var inviteesStr = ""
    var attendees = ["You"];
    meetingDetailsObj.attendees.forEach(function(attendee) {
        let name = (attendee.firstName ? attendee.firstName + (attendee.lastName ? " " : "") : "") + attendee.lastName;
        attendees.push(name);
    });

    if (attendees.length === 4) {
        inviteesStr = renderArrays(attendees);
    } else if (attendees.length > 3) {
        inviteesStr = renderArrays(attendees.slice(0, 3)) + " and " + (attendees.length - 3) + " more";
    } else {
        inviteesStr = renderArrays(attendees);
    }

    //Process date time
    var dateTimeStr = "";
    meetingDetailsObj.slots.forEach(function(slot, i) {
        dateTimeStr += koreUtil.moment(slot.start).calendar(null, calendarFormatList) + " to " + koreUtil.moment(slot.end).format("h:mm a");
        dateTimeStr += (i !== (meetingDetailsObj.slots.length - 1) ? "\n\t\t\t" : "");
    });


    //Generate message details
    var details = "*".concat(meetingDetailsObj.title,
        "*\n\n*:calendar: \t ",
        dateTimeStr);

    if (meetingDetailsObj.mType) {
        details = details.concat(" * \n\n *:beginner: \t",
            meetingDetailsObj.mType);
    }

    details = details.concat(" * \n\n *:bust_in_silhouette: \t",
        inviteesStr,
        "*");
    //Generate message
    var message = {
        "blocks": [{
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": msg
                }
            },
            {
                "type": "divider"
            },
            {
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": details
                }
            }
        ]
    };

    return message;
}

//Regular slack message template

function slackMessage(msg) {
    "use strict";
    var message = {
        "blocks": [{
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": msg
            }
        }]
    }

    return message;
}

function slackContactTemplate(msg, userDetail) {
    "use strict";

    var topText = ""
    var nameText = "";

    if (msg) {
        topText += msg;
    }
    var name = "";

    if (userDetail.fN) {
        name += userDetail.fN;
    }

    if (userDetail.lN) {
        if (name) {
            name += " " + userDetail.lN;
        } else {
            name = userDetail.lN;
        }
    }

    nameText += name + (userDetail.jobTitle ? " - " + userDetail.jobTitle : "");

    if (userDetail.contactUrl) {
        nameText = "*<" + userDetail.contactUrl + "|" + nameText + ">*";
    }

    topText += "\n\n" + nameText;

    var fields = [];

    if (userDetail.email) {
        fields.push({
            "type": "mrkdwn",
            "text": "*Email:*\n" + userDetail.email
        });
    }
    if (userDetail.phone && userDetail.phone.length) {
        fields.push({
            "type": "mrkdwn",
            "text": "*Phone:*\n" + userDetail.phone[0]
        });
    }

    var message = {
        "blocks": [{
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": topText
                }
            },
            {
                "type": "section",
                "fields": fields
            }
        ]
    };

    return message;
}

function slackDailyRoutineTemplate(elements) {
    "use strict";

    var weatherText = "\_" + elements.weather.temp + ", " + elements.weather.desc + "\_";

    var message = {
        "blocks": [{
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": elements.title + ":\n\n\_" + elements.message + "\_" + "\n" + weatherText
                },
                "accessory": {
                    "type": "image",
                    "image_url": elements.weather.icon,
                    "alt_text": elements.weather.temp + ", " + elements.weather.desc
                }
            },
            {
                "type": "divider"
            },
        ]
    }

    if (elements.actionItems) {
        elements.actionItems.forEach(function(item) {
            let itemView = {
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": "\n" + item.title,
                },
            }

            if (item.payload) {
                itemView.accessory = {
                    "type": "button",
                    "text": {
                        "type": "plain_text",
                        "emoji": true,
                        "text": "View",
                    },
                    "style": "primary",
                    "value": String(item.payload)
                }
            }

            message.blocks.push(itemView);
            message.blocks.push(slackDivider());
        })
    }

    return message;
}


//Simple function to insert a divider
function slackDivider() {
    'use strict'

    var divider = {
        "type": "divider"
    }
    return divider
}

function prepareDriveItems(de) {
    var blocks = []
    for (var i = 0; i < de.length; i++) {
        obj = de[i]
        blocks.push({
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "*<{link}|{filename}>*\nShared by {owner}\nLast Edited {lmodified}".format({
                    link: obj.buttons[0].customData.redirectUrl.dweb,
                    filename: obj.fileName,
                    owner: obj.sharedBy,
                    lmodified: koreUtil.moment(obj.lastModified).format("llll")
                })
            },
            //  "accessory": {
            //      "type": "image",
            //      "image_url": obj.thumbnailLink, //obj.iconLink,
            //      "alt_text": "file"
            //  }
        })
        //  blocks.push({
        //                  "type": "divider"
        //              })
    }
    return blocks;
}


function prepareEmailItems(ei) {
    var blocks = []
    for (var i = 0; i < ei.length; i++) {
        obj = ei[i]
        attachmentsCount = obj.attachments.length || 0;
        fmData = {
            link: obj.buttons[0].customData.redirectUrl.dweb,
            subject: obj.subject,
            from: obj.from,
            attachments: (attachmentsCount > 0 ? "\n:paperclip: {num} attachments".format({
                "num": obj.attachments.length || 0
            }) : ""),
            desc: obj.desc
        }
        blocks.push({
            "type": "section",
            "text": {
                "type": "mrkdwn",
                "text": "*<{link}|{subject}>*\nFrom {from}\n{desc}\n{attachments}".format(fmData)
            },
        })
        blocks.push(slackDivider());
    }
    return blocks;
}


/* Templates for MS Teams */

//MS Teams Message Wrapper

function msTeamsWrapper(content) {
    "use strict";

    var message = {
        "type": "message",
        "speak": "",
        "attachments": [{
            "contentType": "application/vnd.microsoft.card.adaptive",
            "content": content,
        }],
    };

    return message;
}

//Simple message template

function msTeamsMessage(msg) {

    "use strict";

    var message = {
        "type": "message",
        "speak": "",
        "attachments": [{
            "contentType": "application/vnd.microsoft.card.adaptive",
            "content": {},
        }],
    };

    message.attachments[0].content = {
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "type": "AdaptiveCard",
        "version": "1.2",
        "body": [{
            "type": "TextBlock",
            "text": msg,
            "wrap": true,
        }],
        "padding": "None"
    };

    return message;
}

//MS Teams quick replies

function msTeamsQR(msg, quickReplyArr) {
    "use strict";

    var message = {
        "type": "message",
        "speak": "",
        "attachments": [{
            "contentType": "application/vnd.microsoft.card.adaptive",
            "content": {},
        }],
    };

    var content = {
        "type": "AdaptiveCard",
        "body": [{
            "type": "Container",
            "padding": "Default",
            "items": [{
                "type": "TextBlock",
                "size": "medium",
                "text": msg,
                "wrap": true
            }],
            "spacing": "none",
            "separator": true
        }, ],
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.2",
        "padding": "None"
    };

    var quickReplies = [];
    quickReplyArr.forEach(function(quickReply, i) {
        let option = {
            "type": "Action.Submit",
            "title": quickReply.title,
            "data": {
                "msteams": {
                    "type": "imBack",
                    "text": quickReply.payload || quickReply.value || quickReply.title,
                    "displayText": quickReply.title,
                    "value": quickReply.payload || quickReply.value || quickReply.title,
                }
            }
        };
        quickReplies.push(option);

        if ((i + 1) % 6 === 0) {
            content.body.push({
                "type": "Container",
                "padding": {
                    "top": "None",
                    "bottom": "Default",
                    "left": "Default",
                    "right": "Default"
                },
                "items": [{
                    "type": "ActionSet",
                    "actions": quickReplies,
                }],
                "spacing": "None"
            });
            quickReplies = [];
        }
    });
    if (quickReplies.length > 0) {
        content.body.push({
            "type": "Container",
            "padding": {
                "top": "Default",
                "bottom": "Default",
                "left": "Default",
                "right": "Default"
            },
            "items": [{
                "type": "ActionSet",
                "actions": quickReplies,
            }],
            "spacing": "None"
        });
    }
    message.attachments[0].content = content;

    return message;
}

//MS Teams Confirmation buttons

function msTeamsConfirmation(msg) {
    "use strict";

    var buttons = [{
            'title': 'Yes',
            'payload': 'yes'
        },
        {
            'title': 'No',
            'payload': 'no'
        }
    ];

    return msTeamsQR(msg, buttons);

}

//MS Teams Time Slot Buttons Template

function msTeamsTimeSlotButtons(msg, slots, otherOptions, extendedSlots) {
    "use strict";

    var content = {
        "type": "AdaptiveCard",
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.2",
        "body": [{
            "type": "TextBlock",
            "text": msg,
            "wrap": true,
            "separator": true
            }],
    };

    var slotButtons = [];
    slots.forEach(function(slot) {

        let buttonPayload = slot.payload || slot.value;

        let button = {
            "type": "Container",
            "items": [
                {
                    "type": "ActionSet",
                    "horizontalAlignment": "Center",
                    "actions": [
                        {
                            "type": "Action.Submit",
                            "title": slot.title,
                            "data": {
                                "msteams": {
                                    "type": "imBack",
                                    "text": buttonPayload,
                                    "displayText": slot.title,
                                    "value": buttonPayload,
                                }
                            }
                        }
                    ]
                }
            ]
        };

        content.body.push(button);
    });

    if (extendedSlots && extendedSlots.length) {
        var dropdown = {
            "type": "Container",
            "items": [
                {
                    "type": "ColumnSet",
                    "columns": [
                        {
                            "type": "Column",
                            "width": "stretch",
                            "horizontalAlignment": "Center",
                            "items": [
                                {
                                    "type": "Input.ChoiceSet",
                                    "choices": [],
                                    "placeholder": "Other options",
                                }
                            ]
                        }
                    ]
                }
            ],
            "separator": true
        };

        var choices = [];
        extendedSlots.forEach(function(day) {
            day.slots.forEach(function(slot) {
                let slotTitle = koreUtil.moment(slot.start).calendar(null, calendarFormatList) + " to " + koreUtil.moment(slot.end).format("h:mm a");
                let option = {
                    "title": slotTitle,
                    "value": koreUtil.moment(slot.start).calendar(null, calendarFormatList),
                };
                choices.push(option);
            });

        });
        dropdown.items[0].columns[0].items[0].choices = choices;
        content.body.push(dropdown);
    }

    return msTeamsWrapper(content);

}

//MS Teams Daily Routines Card

function msTeamsDailyRoutines(elements) {
    "use strict";

    var ImageURL = "";

    var message = {
        "type": "message",
        "speak": "",
        "attachments": [{
            "contentType": "application/vnd.microsoft.card.adaptive",
            "content": {},
        }],
    };

    var weatherText = "\_" + elements.weather.temp + ", " + elements.weather.desc + "\_";

    var content = {
        "type": "AdaptiveCard",
        "body": [{
            "type": "Container",
            "padding": "Default",
            "items": [{
                "type": "ColumnSet",
                "columns": [{
                        "type": "Column",

                        "padding": "None",
                        "width": "auto",
                        "items": [{
                                "type": "TextBlock",
                                "size": "Medium",
                                "weight": "Bolder",
                                "text": elements.title,
                                "spacing": "Small",
                                "wrap": true
                            },
                            {
                                "type": "TextBlock",
                                "text": elements.message,
                                "wrap": true,
                                "spacing": "None",
                            },
                            {
                                "type": "TextBlock",
                                "text": weatherText,
                                "wrap": true,
                                "spacing": "None",
                                "isSubtle": true,
                            },
                        ]
                    },
                    {
                        "type": "Column",
                        "padding": "None",
                        "width": "auto",
                        "items": [{
                            "type": "Image",
                            "url": elements.weather.icon,
                            "horizontalAlignment": "Right",
                            "style": "Person",
                            "size": "Medium",
                            "spacing": "None"
                        }],
                        "horizontalAlignment": "Right"
                    }
                ],
                "padding": "None",
                "horizontalAlignment": "Center"
            }],
            "spacing": "None",
            "separator": true
        }],
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.2",
        "padding": "None"
    };

    if (elements.actionItems) {
        elements.actionItems.forEach(function(item) {
            let summaryItem = {
                "type": "ColumnSet",
                "columns": [
                    {
                        "type": "Column",

                        "padding": "None",
                        "width": "auto",
                        "items": [{
                            "type": "TextBlock",
                            "text": item.title,
                            "wrap": true
                        }]
                    }
                ],
                "padding": "None",
            };

            if(item.payload) {
                summaryItem.selectAction = {
                    "type": "Action.Submit",
                    "data": {
                        "msteams":{
                            "type": "imBack",
                            "text": item.payload,
                            "displayText": item.title,
                            "value": item.payload,
                        }
                    }
                };
            }

            content.body[0].items[0].columns[0].items.push(summaryItem);
        });
    }

    message.attachments[0].content = content;

    return message;

}

//MS Teams meeting confirmation

function msTeamsMeetingConfirmation(msg, meetingDetailsObj) {
    "use strict";

    var inviteesStr = "";
    var attendees = ["You"];
    meetingDetailsObj.attendees.forEach(function(attendee) {
        let name = getPersonFullName(attendee);
        attendees.push(name);
    });

    if (attendees.length === 4) {
        inviteesStr = renderArrays(attendees);
    } else if (attendees.length > 3) {
        inviteesStr = renderArrays(attendees.slice(0, 3)) + " and " + (attendees.length - 3) + " more";
    } else {
        inviteesStr = renderArrays(attendees);
    }

    //Process date time
    var dateTimeStr = "";
    meetingDetailsObj.slots.forEach(function(slot, i) {
        dateTimeStr += koreUtil.moment(slot.start).calendar(null, calendarFormatList) + " to " + koreUtil.moment(slot.end).format("h:mm a");
        dateTimeStr += (i !== (meetingDetailsObj.slots.length - 1) ? "\n\t\t\t" : "");
    });


    var message = {
        "type": "message",
        "speak": "",
        "attachments": [{
            "contentType": "application/vnd.microsoft.card.adaptive",
            "content": {},
        }],
    };

    var content = {
        "type": "AdaptiveCard",
        "body": [{
            "type": "TextBlock",
            "text": msg,
            "wrap": true,
            "separator": true
            },
            {
                "type": "TextBlock",
                "size": "medium",
                "weight": "bolder",
                "text": meetingDetailsObj.title,
                "wrap": true
            },
            {
                "type": "ColumnSet",
                "columns": [
                    {
                        "type": "Column",
                        "items": [{
                            "type": "TextBlock",
                            "spacing": "None",
                            "text": dateTimeStr,
                            "isSubtle": true,
                            "wrap": true
                        }],
                        "width": "stretch"
                    }
                ]
            },
            {
                "type": "ColumnSet",
                "columns": [
                    {
                        "type": "Column",
                        "items": [{
                            "type": "TextBlock",
                            "spacing": "None",
                            "text": inviteesStr,
                            "isSubtle": true,
                            "wrap": true
                        }],
                        "width": "stretch"
                    }
                ]
            }
        ],
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.2"
    };

    if (meetingDetailsObj.mType) {
        content.body.push({
            "type": "ColumnSet",
            "columns": [{
                    "type": "Column",
                    "items": [{
                        "type": "Image",
                        "style": "Person",
                        "url": "${creator.profileImage}",
                        "size": "Small"
                    }],
                    "width": "auto"
                },
                {
                    "type": "Column",
                    "items": [{
                        "type": "TextBlock",
                        "spacing": "None",
                        "text": meetingDetailsObj.mType,
                        "isSubtle": true,
                        "wrap": true
                    }],
                    "width": "stretch"
                }
            ]
        });
    }

    message.attachments[0].content = content;

    return message;

}

//MS Teams elect meeting template

function msTeamsMeetingSelectTemplate(msg, meetingsObj, displayOnly) {
    "use strict";

    var message = {
        "type": "message",
        "speak": "",
        "attachments": [{
            "contentType": "application/vnd.microsoft.card.adaptive",
            "content": {},
        }],
    };

    var content = {
        "type": "AdaptiveCard",
        "body": [{
            "type": "Container",
            "padding": "Default",
            "items": [{
                "type": "TextBlock",
                "size": "medium",
                "weight": "bolder",
                "text": msg,
                "wrap": true
            }],
            "spacing": "none",
            "separator": true
        }],
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.2",
        "padding": "None"
    };

    meetingsObj.forEach(function(meeting, i) {

        let attendeesArr = [];
        meeting.attendees.forEach(attendee => attendeesArr.push("[" + (attendee.name || attendee.email) + "]" + "(" + attendee.email + ")"));

        if (attendeesArr.length > 4) {
            let othersLen = attendeesArr.length - 3;
            attendeesArr = attendeesArr.slice(0, 3);
            attendeesArr.push(othersLen + " others");
        }

        let attendeesStr = renderArrays(attendeesArr);

        let meetingButton = {
            "type": "Container",
            "padding": "Default",
            "items": [{
                "type": "ColumnSet",
                "columns": [{
                        "type": "Column",
                        "padding": "None",
                        "width": "auto",
                        "style": "emphasis",
                        "items": [{
                            "type": "Container",
                            "padding": "None",
                            "items": [{
                                    "type": "TextBlock",
                                    "text": koreUtil.moment(meeting.duration.start).format("dddd"),
                                    "wrap": true
                                },
                                {
                                    "type": "TextBlock",
                                    "text": koreUtil.moment(meeting.duration.start).format("h:mm a"),
                                    "wrap": true
                                },
                                {
                                    "type": "TextBlock",
                                    "text": "to " + koreUtil.moment(meeting.duration.end).format("h:mm a"),
                                    "wrap": true
                                }
                            ]
                        }]
                    },
                    {
                        "type": "Column",
                        "padding": "None",
                        "width": "stretch",
                        "horizontalAlignment": "Left",
                        "items": []
                    },
                ],
                "padding": "None"
            }],
            "spacing": "None",
            "separator": true,
            "horizontalAlignment": "Center"
        };

        var details = [{
                "type": "TextBlock",
                "text": meeting.title,
                "wrap": true
            },
            {
                "type": "TextBlock",
                "text": attendeesStr,
                "wrap": true
            }
        ];

        if (meeting.where) {
            
            if(meeting.where.startsWith("http")) {
                details.push({
                    "type": "TextBlock",
                    "text": "[" + meeting.where + "](" + meeting.where + ")",
                    "wrap": true
                });
            } else {
                details.push({
                    "type": "TextBlock",
                    "text": meeting.where,
                    "wrap": true
                });
            }
        }

        meetingButton.items[0].columns[1].items = details;

        if (!displayOnly) {
            meetingButton.items[0].selectAction = {
                "type": "Action.Submit",
                "title": meeting.title,
                "data": {
                    "msteams": {
                        "type": "imBack",
                        "text": meeting.eventId,
                        "displayText": meeting.title,
                        "value": meeting.eventId
                    }
                }
            };
        }
        content.body.push(meetingButton);
    });

    message.attachments[0].content = content;

    return message;
}

//MS Teams Contact Card

function msTeamsContactCard(msg, userDetail) {
    "use strict";

    var topText = "";
    var nameText = "";

    if (msg) {
        topText += msg;
    }

    var name = "";

    if (userDetail.fN) {
        name += userDetail.fN;
    }

    if (userDetail.lN) {
        if (name) {
            name += " " + userDetail.lN;
        } else {
            name = userDetail.lN;
        }
    }

    nameText += name + (userDetail.jobTitle ? " - " + userDetail.jobTitle : "");

    var fields = "";

    if (userDetail.phone && userDetail.phone.length) {
        fields += "*Phone:*\n" + userDetail.phone[0];
    }

    var ImageURL = "";

    var content = {
        "type": "AdaptiveCard",
        "body": [
            {
                "type": "TextBlock",
                "text": topText,
                "wrap": true
            },
            {
                "type": "Container",
                "style": "emphasis",
                "items": [
                    // {
                    //     "type": "Container",
                    //     "padding": "None",
                    //     "items": [{
                    //         "type": "Image",
                    //         "url": ImageURL,
                    //         "horizontalAlignment": "center",
                    //         "style": "person",
                    //     }]
                    // },
                    {
                        "type": "Container",
                        "padding": "None",
                        "items": [{
                            "type": "TextBlock",
                            "text": nameText,
                            "wrap": true,
                            "horizontalAlignment": "center",
                            "weight": "Bolder",
                        }]
                    }
                ],
                "padding": "Default",
                "selectAction": {
                    "type": "Action.OpenUrl",
                    "url": userDetail.contactUrl,
                    "title": nameText,
                }
            },
            {
                "type": "Container",
                "padding": "Default",
                "items": [
                    {
                        "type": "Container",
                        "padding": "None",
                        "items": [{
                            "type": "TextBlock",
                            "text": "*Email:* \n " + userDetail.email,
                            "wrap": true
                        }],
                        "selectAction": {
                            "type": "Action.OpenUrl",
                            "url": userDetail.emailUrl,
                            "title": userDetail.emailUrl,
                        }
                    },
                    {
                        "type": "ColumnSet",
                        "columns": [{
                                "type": "Column",
                                "padding": "None",
                                "width": "stretch",
                                "items": [{
                                    "type": "TextBlock",
                                    "text": fields,
                                    "wrap": true,
                                }]
                            },
                        ],
                        "padding": "None",
                    },
                ],
                "spacing": "None",
                "separator": true,
                "horizontalAlignment": "Center"
            }
        ],
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.2",
        "padding": "None"
    };

    return msTeamsWrapper(content);

}

//MS Teams Task Template

function msTeamsTaskTemplate(msg, taskData) {
    "use strict";

    var ImageURL = "";

    var message = {
        "type": "message",
        "speak": "",
        "attachments": [{
            "contentType": "application/vnd.microsoft.card.adaptive",
            "content": {},
        }],
    };

    var content = {
        "type": "AdaptiveCard",
        "body": [{
            "type": "TextBlock",
            "text": msg,
            "wrap": true,
            "separator": true
        }],
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.2",
        "padding": "None"
    };

    if (Array.isArray(taskData)) {
        var taskInfo = [];
        taskData.forEach(function(task) {

            let taskTitleInfo = {
                "type": "Container",
                "padding": "Default",
                "items": [{
                    "type": "TextBlock",
                    "text": "\_Title:\_ " + task.title,
                    "wrap": true,
                    "separator": true,
                    "color": "Accent",
                }],
                "spacing": "None",
                "separator": true
            };
            taskInfo.push(taskTitleInfo);

            let taskTimeInfo = {
                "type": "Container",
                "padding": "Default",
                "items": [{
                    "type": "Container",
                    "padding": "None",
                    "items": [{
                        "type": "ColumnSet",
                        "columns": [
                            {
                                "type": "Column",
                                "padding": "None",
                                "width": "auto",
                                "separator": true,
                                "color": "Accent",
                                "items": [{
                                    "type": "TextBlock",
                                    "text": "\_Due:\_ " + koreUtil.moment(task.dueDate).format("MMMM Do YYYY, h:mm:ss a"),
                                    "wrap": true
                                }]
                            }
                        ],
                        "padding": "None"
                    }]
                }],
                "separator": true,
                "spacing": "None"
            };
        });

        taskInfo.push(taskTimeInfo);

        let taskAssigneeInfo = {
            "type": "Container",
            "padding": "Default",
            "items": [{
                "type": "Container",
                "padding": "None",
                "items": [{
                    "type": "ColumnSet",
                    "columns": [
                        {
                            "type": "Column",
                            "padding": "None",
                            "width": "auto",
                            "separator": true,
                            "color": "Accent",
                            "items": [{
                                "type": "TextBlock",
                                "text": "\_Assignee:\_ " + getPersonFullName(task.assignee),
                                "wrap": true
                            }]
                        }
                    ],
                    "padding": "None"
                }]
            }],
            "separator": true,
            "spacing": "None"
        };

        taskInfo.push(taskAssigneeInfo);

    } else {
        var taskInfo = [{
                "type": "Container",
                "padding": "Default",
                "items": [{
                    "type": "TextBlock",
                    "text": "\_Title:\_ " + (taskData && taskData.title),
                    "separator": true,
                    "color": "Accent",
                    "wrap": true
                }],
                "spacing": "None",
                "separator": true
            },
            {
                "type": "Container",
                "padding": "Default",
                "items": [{
                    "type": "Container",
                    "padding": "None",
                    "items": [{
                        "type": "ColumnSet",
                        "columns": [
                            {
                                "type": "Column",
                                "padding": "None",
                                "width": "auto",
                                "items": [{
                                    "type": "TextBlock",
                                    "separator": true,
                                    "color": "Accent",
                                    "text": "\_Due:\_ " + koreUtil.moment(taskData.dueDate).format("MMMM Do YYYY, h:mm:ss a"),
                                    "wrap": true
                                }]
                            }
                        ],
                        "padding": "None"
                    }]
                }],
                "separator": true,
                "spacing": "None"
            },
            {
                "type": "Container",
                "padding": "Default",
                "items": [{
                    "type": "Container",
                    "padding": "None",
                    "items": [{
                        "type": "ColumnSet",
                        "columns": [
                            {
                                "type": "Column",
                                "padding": "None",
                                "width": "auto",
                                "items": [{
                                    "type": "TextBlock",
                                    "separator": true,
                                    "color": "Accent",
                                    "text": "\_Assignee:\_ " + getPersonFullName(taskData.assignee),
                                    "wrap": true
                                }]
                            }
                        ],
                        "padding": "None"
                    }]
                }],
                "separator": true,
                "spacing": "None"
            }
        ];
    }

    content.body = content.body.concat(taskInfo);
    message.attachments[0].content = content;

    return message;

}

//MS Teams Drive Lookup Template

function msTeamsDriveLookup(msg, FileDataArr) {
    "use strict";

    var message = {
        "type": "message",
        "speak": "",
        "attachments": [{
            "contentType": "application/vnd.microsoft.card.adaptive",
            "content": {},
        }],
    };

    var content = {
        "type": "AdaptiveCard",
        "body": [{
            "type": "Container",
            "padding": "Default",
            "items": [{
                "type": "TextBlock",
                "size": "medium",
                "text": msg,
                "wrap": true
            }],
            "spacing": "none",
            "separator": true
        }],
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "version": "1.2"
    };


    var container = {
        "type": "Container",
        "items": []
    };


    FileDataArr.forEach(function(oneFile) {
        var _owner = oneFile.owners && oneFile.owners[0] && oneFile.owners[0].displayName;
        container.items.push({
            "type": "ColumnSet",
            "columns": [{
                "type": "Column",
                "width": "stretch",
                "items": [{
                    "type": "ColumnSet",
                    "columns": [{
                            "type": "Column",
                            "items": [{
                                "type": "Image",
                                "url": oneFile.icon,
                                "size": "Medium"
                            }],
                            "width": "auto"
                        },
                        {
                            "type": "Column",
                            "items": [{
                                    "type": "TextBlock",
                                    "weight": "Bolder",
                                    "text": oneFile.name,
                                    "wrap": true
                                },
                                {
                                    "type": "TextBlock",
                                    "spacing": "None",
                                    "text": "Shared by " + _owner,
                                    "isSubtle": true,
                                    "wrap": true
                                },
                                {
                                    "type": "TextBlock",
                                    "text": "Last Edited " + koreUtil.moment(oneFile.modifiedTime).format("ddd, MMM DD, YYYY"),
                                    "wrap": true,
                                    "spacing": "None",
                                    "isSubtle": true
                                }
                            ],
                            "width": "stretch"
                        }
                    ]
                }],
                "separator": true
            }],
            "separator": true,
            "selectAction": {
                "type": "Action.OpenUrl",
                "url": oneFile.webViewLink
            }
        });
    });

    message.attachments[0].content = content;
    message.attachments[0].content.body.push(container);


    return message;
}


/* Common array functions */

// Render a simple string array properly with commas, spacing and an 'and' before the last item.
function renderArrays(arr, notAnd) {
    "use strict";
    var text = arr[0];
    if (arr.length > 1) {
        for (var i = 1; i < arr.length - 1; ++i) {
            text += ", " + arr[i];
        }
        var conjunction = "";
        if (arr.length > 2) {
            text += ", ";
        } else if (arr.length === 2) {
            conjunction += notAnd ? ", " : " ";
        }
        conjunction += notAnd ? "" : "and ";
        text += conjunction + arr[arr.length - 1];
    }
    return text;
}


// Return array without duplicates
function findDuplicatesInArray(arr) {
    "use strict";

    var object = {};
    var result = [];

    arr.forEach(function(item) {
        if (!object[item]) {
            object[item] = 0;
        }
        object[item] += 1;
    });

    for (var prop in object) {
        if (object[prop] >= 2) {
            result.push(prop);
        }
    }

    return result;
}


// Convert an object into a set
function insertToSet(obj) {
    "use strict";

    var nSet = new Set();
    obj.forEach(function(element) {
        nSet.add(element.name)
    })
    return nSet;
}

/* END */



/* Common Date Utility Functions*/

// **** Most of these functions can be replaced by Moment calls

//function accepts number of minutes and returns a sentence in terms of hours and minutes
function minsToTimeSentence(mins) {
    var hours = Math.floor(mins / 60);
    var minutes = mins % 60;
    var hourSentence = hours ? hours + (minutes !== 30 ? (" hour" + (hours > 1 ? "s" : "")) : "") : "";
    var minuteSentence = minutes === 30 ? "a ½ hour" + (hours ? "s" : "") : minutes ? minutes + " minute" + (minutes > 1 ? "s" : "") : "";
    var timeSentence = hourSentence + (hours && minutes ? " and " : "") + minuteSentence;
    return timeSentence;
}

//function accepts a date period object (containing 'fromDate' and 'toDate') and returns an appropriate sentence for the date period depending on the current date. Accounts for DST.
function datePeriodToPhrase(datePeriod, pod = '') {
    str = "";

    calendarFormatDateOnly = {
        sameDay: '[today], MMMM Do' + ' [' + pod + ']',
        nextDay: '[tomorrow], MMMM Do' + ' [' + pod + ']',
        nextWeek: 'dddd, MMM Do' + ' [' + pod + ']',
        lastDay: '[yesterday], MMMM Do' + ' [' + pod + ']',
        lastWeek: '[last] dddd, MMMM Do' + ' [' + pod + ']',
        sameElse: 'dddd, MMMM Do, YYYY' + ', [' + pod + ']'
    };

    startMoment = koreUtil.moment(datePeriod.fromDate, "YYYY-MM-DD");
    endMoment = koreUtil.moment(datePeriod.toDate, "YYYY-MM-DD");
    currMoment = koreUtil.moment();
    if (startMoment.isDST() && !endMoment.isDST()) {
        startMoment.add(1, 'hour');
    } else if (endMoment.isDST() && !startMoment.isDST()) {
        endMoment.add(1, 'hour');
        if (!currMoment.isDST()) {
            currMoment.add(1, 'hour');
        }
    }

    diffDays = (koreUtil.moment.duration(endMoment.diff(startMoment))).days();
    diffWeeks = koreUtil.moment.duration(endMoment.clone().startOf('isoweek').diff(startMoment.clone().startOf('isoweek'))).weeks();
    if (startMoment.isDST() && !currMoment.isDST()) {
        diffStartWeek = koreUtil.moment.duration(startMoment.clone().startOf('isoweek').add(1, 'hour').diff(currMoment.clone().startOf('isoweek'))).weeks();
    } else if (currMoment.isDST() && !startMoment.isDST()) {
        diffStartWeek = koreUtil.moment.duration(startMoment.clone().startOf('isoweek').diff(currMoment.clone().startOf('isoweek').add(1, 'hour'))).weeks();
    } else {
        diffStartWeek = koreUtil.moment.duration(startMoment.clone().startOf('isoweek').diff(currMoment.clone().startOf('isoweek'))).weeks();
    }

    diffMonth = startMoment.month() - currMoment.month();
    isWeek = (diffWeeks === 0 && diffDays === 6) ? true : false;
    isMonth = (diffDays + 1 === startMoment.daysInMonth()) ? true : false;
    if (isWeek && diffStartWeek === 1) {
        str += "next week";
    } else if (diffWeeks === 0 && endMoment.day() === 0 && diffStartWeek === 0) {
        str += "this week";
    } else if (isWeek && diffStartWeek === -1) {
        str += "last week";
    } else if ((diffMonth === 0 && startMoment.date === 1) || (currMoment.month() === endMoment.month() && endMoment.date() === currMoment.daysInMonth())) {
        str += "this month";
    } else if (isMonth && diffMonth === 1) {
        str += "next month";
    } else if (isMonth && diffMonth === -1) {
        str += "last month";
    } else if (isMonth) {
        str += " in " + startMoment.format("MMMM");
    } else {
        str += "between " + koreUtil.moment(datePeriod.fromDate).calendar(null, calendarFormatDateOnly);
        str += " and " + koreUtil.moment(datePeriod.toDate).calendar(null, calendarFormatDateOnly);
    }
    return str
}

function getOffSetToMins(offset) {
    "use strict";

    var _off = offset.split(':');
    var hour = Number(_off[0]);
    var minutes = Number(_off[1]);
    return -(hour * 60 + minutes);
}

function getTimeOfDate(date, timeMins, zone) {
    "use strict";

    var onDay = new Date(date);
    var zoneOffset = koreUtil.moment.tz(zone).format('Z');
    var offsetMins = getOffSetToMins(zoneOffset);
    return onDay.getTime() + ((timeMins + offsetMins) * 60000);
}

function getTimeofDateTime(dateTime, zone) {
    "use strict";

    return new Date(koreUtil.moment(dateTime).tz(zone)).getTime();
}

function getDateFormatByZone(dateObj, timeZone, format) {
    "use strict";

    return koreUtil.moment(dateObj).tz(timeZone).format(format);
}


// Return the user's current timezone
function getUserCurrentTimeZone() {
    "use strict";
    // 1. websdk channel 2) webhook channel
    var contextMeta = getMessagePayloadData("meta") || getUserCurrentTimeZoneForWebHook() || {};
    var customData = getMessagePayloadData("botInfo.customData");
    var timeZone = (customData && customData.KATZ) || contextMeta.timezone || env.KATimezone;
    return timeZone;
}

// Return the user's current timezone
function getUserCurrentTimeZoneForWebHook() {
    "use strict";

    var textPayload = getMessagePayloadData("text");
    if (textPayload && textPayload.length > 0) {
        try {
            return JSON.parse(textPayload[0]);
        } catch {
            return "America/New_York"
        }
    }
}


// Add date centric synonyms to a LoV item
function addDateSynonyms(synonyms, dateval) {
    "use strict";

    var date = koreUtil.moment(dateval);

    // user could enter any of these combinations
    // long names: Thursday September 4th 1986
    // short names: Thu Sep 4 86
    var dateStr = date.format('dddd MMMM Do YYYY LT ddd MMM D YY h m a');
    synonyms.push(dateStr);

    dateStr = date.calendar(); // easy relative times: today, tomorrow, mm/dd/yyyy
    synonyms.push(dateStr);
}

/* END */



/* Common email/person utility functions */

//Function to fetch email ID based on User name.
function getUserEmailId(userContext) {
    "use strict";

    // might not have been passed a context
    if (!userContext || typeof(userContext) !== "object") {
        userContext = context.session.UserContext;
    }
    /*
        Incase of web hook channel email id will be not available in identity
        So we are setting email id in set test user dialog if user is providing
        durring setting the set test use

    */
    var userSession = context.session.UserSession || {};
    var customData = getMessagePayloadData("botInfo.customData");
    var emailId = (customData && customData.email) || (userContext && userContext.emailId) || userSession.kmEmailId;
    if (!emailId && userContext.identities) {
        userContext.identities.forEach(function(ele) {
            if (ele.type === 'mapped') {
                emailId = ele.val.split('/')[1] || '';
            }
        });
    }
    return emailId
}


// Get the userid used in all API calls
function getUserId(userContext) {
    "use strict";

    var userDetails = getKAUserDetails(userContext);
    return userDetails.userId;
}


// Return the user's first name
function getFirstName(userContext) {
    "use strict";

    var fN = "";
    if (userContext && typeof(userContext) === "object") {
        if (koreUtil._.has(userContext, "customData.fN")) {
            fN = userContext.customData.fN;
        } else {
            fN = userContext.firstName || userContext.fN;
        }
    }
    return fN;
}


// Return the user's last name
function getLastName(userContext) {
    "use strict";

    var lN = "";
    if (userContext && typeof(userContext) === "object") {
        if (koreUtil._.has(userContext, "customData.lN")) {
            lN = userContext.customData.lN;
        } else {
            lN = userContext.lastName || userContext.lN;
        }
    }
    return lN;
}


// Return the user's full name
function getFullName(userContext) {
    "use strict";

    var name = "";
    if (userContext && typeof(userContext) === "object") {
        if (koreUtil._.has(userContext, "customData.name")) {
            name = userContext.customData.name;
        } else if (userContext.name) {
            name = userContext.name;
        } else {
            fname = getFirstName(userContext);
            lname = getLastName(userContext);
            if (fname && lname) {
                name = fname + " " + lname;
            } else if (fname) {
                name = fname
            } else if (lname) {
                name = lname;
            }
        }
    }
    return name;
}

//Return a person's full name

function getPersonFullName(person) {
    "use strict"

    var name = "";

    if (person.fN || person.firstName) {
        name += (person.fN || person.firstName);
    }

    if (person.lN || person.lastName) {
        if (name == "") {
            name = (person.lN || person.lastName);
        } else {
            name += " " + (person.lN || person.lastName);
        }
    }

    return name;
}


// Validate Email with Regex
function validateEmailId(email) {
    "use strict";

    var regex = /^[A-Za-z0-9_+\-\.]+@[A-Za-z0-9_\-\.]+\.[A-Za-z]{2,4}$/;
    return regex.test(email);
}


// Remove words from a text string
function trimWords(text, words) {
    "use strict";

    if (Array.isArray(words)) {
        words.forEach(function(word) {
            var re = new RegExp('\\b' + word + '\\b\s?', 'gi');
            text = text.replace(re, '');
        });
    }
    return text.trim();
}

//Input: Array of User Ids and User Map object of User IDs to User Objects (Or Team Map Object). Output: First Names of all users with IDs in the array.
function renderNames(arr, userData, teamMemberData, exceptAnd) {
    "use strict";

    context.entities.debug.names = [];

    var text = "";
    if (arr.length === 0) {
        return text;
    }
    text = getFirstName(userData[arr[0]] || teamMemberData[arr[0]]);
    //    context.entities.debug.names.push(text);

    if (arr.length > 1) {
        for (var i = 1; i < arr.length - 1; ++i) {
            text = text + ", " + getFirstName(userData[arr[i]] || teamMemberData[arr[i]]);
        }
        text += (exceptAnd ? ", " : " and ") + getFirstName(userData[arr[arr.length - 1]] || teamMemberData[arr[arr.length - 1]]);
    }
    return text;
}

/* END */



/* Security functions */

// tests to see if a user can access an intent, or a specific functional area
// if cannot access the intent, a phrase fragment is returned that can be spliced into a response
// othwerwise return a null to support an Exists transition
function isIntentDisabled(intentName, functionName) {
    // determine the actual ACL name from an intent name 
    var intentDetails;
    if (intentName && !functionName) {
        intentName = intentName.replace(" @development", "");
        intentDetails = KA_TASKACLS[intentName];
        if (intentDetails) {
            functionName = intentDetails.functionName;
        }
    }
    if (!functionName) {
        return null;
    }

    var ACL;

    if (koreUtil._.has(context.session.BotUserSession, "lastMessage.messagePayload.botInfo.customData.appControlList")) {
        ACL = context.session.BotUserSession.lastMessage.messagePayload.botInfo.customData.appControlList;
    }
    if (!ACL && context.session.UserSession.appControlList) {
        ACL = context.session.UserSession.appControlList;
    }
    if (!ACL && env.KADefaultUser) {
        var userDetails = JSON.parse(env.KADefaultUser);
        ACL = userDetails.appControlList;
    }

    if (ACL && typeof(ACL) === "object" && ACL[functionName] === 1) {
        return null;
    }

    // return a specific message, if known
    if (intentDetails) {
        return intentDetails.message;
    }
    return true;
}

function isCompatibleChannel(intentName, channel) {
    var configObj = KA_TASKACLS[intentName] || {};
    if (channel === "slack") {
        return (configObj.slackSupport !== undefined ? configObj.slackSupport : true); // assume true by default
    }
    return true; // no troubles
}

var KA_TASKACLS = {
    "AddAnnouncement": {
        "functionName": "ENABLE_ANNOUNCEMENT",
        "message": "create an announcement",
        "slackSupport": false
    },
    "AddInvitee": {
        "functionName": "ENABLE_MEETINGS",
        "message": "add an invitee to a meeting",
        "slackSupport": true
    },
    "AddKnowledge": {
        "functionName": "ENABLE_KNOWLEDGE",
        "message": "create an article",
        "slackSupport": false
    },
    "AddMeetingNotes": {
        "functionName": "ENABLE_MEETINGS",
        "message": "add notes to a meeting",
        "slackSupport": false
    },
    "BlockMyCalendar": {
        "functionName": "ENABLE_MEETINGS",
        "message": "schedule a meeting",
        "slackSupport": true
    },
    "CancelMeeting": {
        "functionName": "ENABLE_MEETINGS",
        "message": "cancel a meeting",
        "slackSupport": true
    },
    "ChangeMeeting": {
        "functionName": "ENABLE_MEETINGS",
        "message": "change a meeting",
        "slackSupport": true
    },
    "ChangeMeetingTitle": {
        "functionName": "ENABLE_MEETINGS",
        "message": "change a meeting",
        "slackSupport": true
    },
    "ChangeMeetingType": {
        "functionName": "ENABLE_MEETINGS",
        "message": "change a meeting",
        "slackSupport": true
    },
    "CreateReminder": {
        "functionName": "ENABLE_TASK",
        "message": "create a reminder",
        "slackSupport": true
    },
    "CreateTask": {
        "functionName": "ENABLE_TASK",
        "message": "create a task",
        "slackSupport": true
    },
    "DisplayFreeSlots": {
        "functionName": "ENABLE_MEETINGS",
        "message": "view your free time",
        "slackSupport": true
    },
    "DisplayMeetings": {
        "functionName": "ENABLE_MEETINGS",
        "message": "view your meetings",
        "slackSupport": true
    },
    "DriveLookup": {
        "functionName": "ENABLE_DRIVE",
        "message": "retrieve documents from your drive",
        "slackSupport": true
    },
    "GetAnnouncement": {
        "functionName": "ENABLE_ANNOUNCEMENT",
        "message": "view announcements",
        "slackSupport": false
    },
    "GetEmail": {
        "functionName": "ENABLE_EMAIL",
        "message": "show your emails",
        "slackSupport": true
    },
    "GetKnowledgeForUser": {
        "functionName": "ENABLE_KNOWLEDGE",
        "message": " view articles",
        "slackSupport": false
    },
    "GetMeetingNotes": {
        "functionName": "ENABLE_MEETINGS",
        "message": "view notes for a meeting",
        "slackSupport": false
    },
    "MeetingLookup": {
        "functionName": "ENABLE_MEETINGS",
        "message": "view your meetings",
        "slackSupport": true
    },
    "ModifyInvitees": {
        "functionName": "ENABLE_MEETINGS",
        "message": "modify the invitees to a meeting",
        "slackSupport": true
    },
    "RemoveAllInvitee": {
        "functionName": "ENABLE_MEETINGS",
        "message": "remove invitees from a meeting",
        "slackSupport": true
    },
    "RemoveInvitee": {
        "functionName": "ENABLE_MEETINGS",
        "message": "remove invitees from a meeting",
        "slackSupport": true
    },
    "RescheduleMeeting": {
        "functionName": "ENABLE_MEETINGS",
        "message": "reschedule a meeting",
        "slackSupport": true
    },
    "RespondToMeeting": {
        "functionName": "ENABLE_MEETINGS",
        "message": "respond to a meeting",
        "slackSupport": true
    },
    "RespondToTask": {
        "functionName": "ENABLE_TASK",
        "message": "respond to a task",
        "slackSupport": true
    },
    "ScheduleMeeting": {
        "functionName": "ENABLE_MEETINGS",
        "message": "schedule a meeting",
        "slackSupport": true
    },
    "SendEmail": {
        "functionName": "ENABLE_EMAIL",
        "message": "send an email",
        "slackSupport": true
    },
    "SkillTransfer": {
        "functionName": "ENABLE_SKILL",
        "message": "perform that skill",
        "slackSupport": true
    },
    "TaskLookup": {
        "functionName": "ENABLE_TASK",
        "message": "view your tasks",
        "slackSupport": true
    },
    "UpdateMeetingDetails": {
        "functionName": "ENABLE_MEETINGS",
        "message": "change a meeting",
        "slackSupport": true
    },
    "UpdateTask": {
        "functionName": "ENABLE_TASK",
        "message": "update a task",
        "slackSupport": true
    }
};


/* END */



/* KA Service Request utilities*/
var KA_APIS = {
    "GetEscalationDept": {
        url: "ka/users/{{context.KAUserId}}/escalations",
        method: "GET",
        node: "KA_GET_Service"
    },
    "SendEscalationMail": {
        url: "ka/users/{{context.KAUserId}}/sendemail/escalation",
        method: "POST",
        node: "KA_POST_Service"
    },
    // Knowledge
    "SearchKnowledgeData": {
        url: "ka/users/{{context.KAUserId}}/knowledge/resolve",
        method: "POST",
        node: "KA_POST_GetKnowledge"
    },
    "ShareArticle": {
        url: "ka/users/{{context.KAUserId}}/knowledge/{{context.KAKnowledgeId}}/share",
        method: "POST",
        node: "KA_POST_Service"
    },
    "Notify_Colleague": {
        url: "ka/users/{{context.KAUserId}}/question",
        method: "POST",
        node: "KA_POST_Service"
    },
    // Contacts
    "PersonResolve": {
        url: "ka/users/{{context.KAUserId}}/search/contacts",
        method: "POST",
        node: "KA_POST_Service"
    },
    "ResolveKoraUser": {
        url: "ka/users/{{context.KAUserId}}/profile/resolve",
        method: "POST",
        node: "KA_POST_Service"
    },
    // CloudSearch
    "GetDriveData": {
        url: "ka/users/{{context.KAUserId}}/search/drive",
        method: "POST",
        node: "KA_POST_Service"
    },
    "GetEmailInfo": {
        url: "ka/users/{{context.KAUserId}}/search/emails",
        method: "POST",
        node: "KA_POST_Service"
    },
    // Teams
    "GetAllTeamForUser": {
        url: "ka/users/{{context.KAUserId}}/teams",
        method: "GET",
        node: "KA_GET_Service"
    },
    // Meetings
    "MeetingSlot": {
        url: "ka/users/{{context.KAUserId}}/calendar/freebusyslots",
        method: "POST",
        node: "KA_POST_Service"
    },
    "CreateEvent": {
        url: "ka/users/{{context.KAUserId}}/calendar/createEvents",
        method: "POST",
        node: "KA_POST_Service"
    },
    "GetMeetingData": {
        url: "ka/users/{{context.KAUserId}}/meetingrequest/{{context.KAMeetingId}}",
        method: "GET",
        node: "KA_GET_Service"
    },
    "UpdateMeetingRequest": {
        url: "ka/users/{{context.KAUserId}}/meetingrequest/{{context.KAMeetingId}}",
        method: "PUT",
        node: "KA_PUT_Service"
    },
    "GetMeetingType": {
        url: "ka/users/{{context.KAUserId}}/profile/meetingType",
        method: "GET",
        node: "KA_GET_Service"
    },
    "SpecificMeetingLookupHook": {
        url: "ka/users/{{context.KAUserId}}/calendar/getSpecificEvent",
        method: "POST",
        node: "KA_POST_Service"
    },
    "MeetingLookupHook": {
        url: "ka/users/{{context.KAUserId}}/calendar/getEvents",
        method: "POST",
        node: "KA_POST_Service"
    },
    "MeetingRequestLookupHook": {
        url: "ka/users/{{context.KAUserId}}/search/meetingrequest",
        method: "POST",
        node: "KA_POST_Service"
    },
    "CancelMeetingHook": {
        url: "ka/users/{{context.KAUserId}}/calendar/cancelEvents",
        method: "POST",
        node: "KA_POST_Service"
    },
    "CancelMeetingRequest": {
        url: "ka/users/{{context.KAUserId}}/meetingrequest/{{context.KAMeetingId}}",
        method: "DELETE",
        node: "KA_DEL_Service"
    },
    "CreateMeetingRequestObj": {
        url: "ka/users/{{context.KAUserId}}/meetingrequest",
        method: "POST",
        node: "KA_POST_Service"
    },
    "RescheduleEventHook": {
        url: "ka/users/{{context.KAUserId}}/calendar/rescheduleEvent",
        method: "POST",
        node: "KA_POST_Service"
    },
    "CheckMeetingNotesDraft": {
        url: "/ka/users/{{context.KAUserId}}/meetings/{{context.mId}}/notes/draft",
        method: "GET",
        node: "KA_GET_SERVICE"
    },
    "CreateMeetingNotes": {
        url: "/ka/users/{{context.KAUserId}}/meetings/{{context.mId}}/notes",
        method: "POST",
        node: "KA_POST_SERVICE"
    },
    "SearchMeetingNotes": {
        url: "/ka/users/{{context.KAUserId}}/search/meetingnotes",
        method: "POST",
        node: "KA_POST_Service"
    },
    "ResolveMeetingNotes": {
        url: "/ka/users/{{context.KAUserId}}/meetings/{{context.mId}}/notes",
        method: "GET",
        node: "KA_GET_SERVICE"
    },
    // Tasks and Reminders
    "AddTask": {
        url: "ka/users/{{context.KAUserId}}/task",
        method: "POST",
        node: "KA_POST_Service"
    },
    "ResolveTask": {
        url: "ka/users/{{context.KAUserId}}/task/resolve",
        method: "POST",
        node: "KA_POST_Service"
    },
    "UpdateTaskState": {
        url: "ka/users/{{context.KAUserId}}/task",
        method: "PUT",
        node: "KA_PUT_Service"
    },
    "AddReminder": {
        url: "ka/users/{{context.KAUserId}}/reminder",
        method: "POST",
        node: "KA_POST_Service"
    },
    "ResolveReminder": {
        url: "ka/users/{{context.KAUserId}}/reminder/resolve",
        method: "POST",
        node: "KA_POST_Service"
    },
    // Skills
    "CheckSkill": {
        url: "ka/users/{{context.KAUserId}}/skills",
        method: "GET",
        node: "KA_GET_Service"
    },
    "OnBoardingInvitees": {
        url: "ka/users/{{context.KAUserId}}/search/contacts/recent?n={{context.ONBInviteeCount}}&onboarding=true",
        method: "GET",
        node: "KA_GET_Service"
    },
    // Others
    "Send_Email_Webhook": {
        url: "ka/users/{{context.KAUserId}}/sendemail",
        method: "POST",
        node: "KA_POST_Service"
    },
    "DailyRoutineHook": {
        url: "ka/users/{{context.KAUserId}}/routine/getAllEvents",
        method: "POST",
        node: "KA_POST_Service"
    },
    "GetUserProfile": {
        url: "ka/users/{{context.KAUserId}}/profile",
        method: "GET",
        node: "KA_GET_Service"
    },
    "GetKoraEntity": {
        url: "ka/users/{{context.KAUserId}}/koraEntity",
        method: "POST",
        node: "KA_POST_Service"
    },
    "GetLastConnHook": {
        url: "ka/users/{{context.KAUserId}}/check/summary",
        method: "POST",
        node: "KA_POST_Service"
    },
    "ClearSTMCache": {
        url: "/ka/users/{{context.KAUserId}}/cache/names",
        method: "DELETE",
        node: "KA_DEL_Service"
    },
    "UniversalSearch": {
        url: "ka/users/{{context.KAUserId}}/search",
        method: "POST",
        node: "KA_POST_Service"
    },
    "ResolveFeedbackData": {
        url: "ka/conf/users/{{context.KAUserId}}/feedback/meta",
        method: "GET",
        node: "KA_GET_Service"
    },
    "CheckSKUUsage": {
        url: "ka/users/{{context.KAUserId}}/checkUsage",
        method: "GET",
        node: "KA_GET_Service"
    },
    "AvailabilityCheck": {
        url: "ka/users/{{context.KAUserId}}/calendar/freeslots",
        method: "POST",
        node: "KA_POST_Service"
    },
    "MeetingRoomAvailability": {
        url: "ka/users/{{context.KAUserId}}/calendar/roomsAvailability",
        method: "POST",
        node: "KA_POST_Service"
    }
}


// Reset the Kora service API call details
function resetKAServiceAPI() {
    "use strict";

    //userDetails
    delete context.KAUserId;
    delete context.KABearer;
    //request body preparation
    delete context.KARequestBody;
    //service request endpoint url
    delete context.serviceAPI;
    //request query parameters
    delete context.serviceQuery;
    //service request node name
    delete context.KAServiceNodeName;
    //service response node name - defaults to userInfo
    delete context.KAResponseNodeName;
    //service specific error message - referenced in ServerError
    delete context.KAErrorMessage;
}


// Set up the Kora service API call
function setKAServiceAPI(apiName, userDetails) {
    "use strict";

    resetKAServiceAPI();
    context.serviceAPI = undefined;
    userDetails = userDetails || getKAUserDetails() || {};
    context.KAUserId = userDetails.userId;
    context.KABearer = "bearer " + userDetails.accessToken;
    context.KAServiceNodeName = KA_APIS[apiName] && KA_APIS[apiName].node;

    context.serviceAPI = KA_APIS[apiName] && KA_APIS[apiName].url;
}


// Get the authorization details for the Kora service request
function getKAUserDetails(userContext) {
    "use strict";

    var userDetails;

    // might have been passed a specific context
    if (!userContext || typeof(userContext) !== "object") {
        userContext = context.session.UserContext;
    }
    var customData;
    //In case of ON_CONNECT Event latest custom data will be available in BotUserSession
    //bcz by this time inside the lastmessage we have old customdata not latest one
    if (context.session.BotUserSession.isOnConnect == 'true' || context.session.BotUserSession.isOnConnect == true) {
        customData = context.session.BotUserSession.customData || {};
    } else if (getMessagePayloadData("botInfo.customData")) {
        customData = getMessagePayloadData("botInfo.customData");
    } else {
        customData = getMessagePayloadData("message.customData");
    }

    //var customData = getMessagePayloadData("message.customData");
    var userSession = context.session.UserSession;

    if (customData && customData.kmUId && customData.kmToken) {
        // will have been set by the Kora client
        userDetails = {
            userId: customData.kmUId,
            accessToken: customData.kmToken
        };
    }
    if (!userDetails && userContext && userContext.customData && userContext.customData.kmUId && userContext.customData.kmToken) {
        // will have been set by the Kora client
        userDetails = {
            userId: userContext.customData.kmUId,
            accessToken: userContext.customData.kmToken
        };
    }
    if (!userDetails && userSession && userSession.kmUId && userSession.kmToken) {
        // will have been set via SetTestUser
        userDetails = {
            userId: userSession.kmUId,
            accessToken: userSession.kmToken
        };
    }
    if (!userDetails && env.KADefaultUser) {
        //TODO Allow only for configured mode/From builder Test 
        userDetails = JSON.parse(env.KADefaultUser);
    }
    return userDetails;
}

/* END */



/* Friendly task names */

var KA_TASKNAMES = {
    "AddAnnoucement": content.AddAnnoucement,
    "AddInvitee": content.AddInvitee,
    "AddKnowledge": content.AddKnowledge,
    "AddMeetingNotes": content.AddMeetingNotes,
    "AskExpert": content.AskExpert,
    "BlockMyCalendar": content.BlockMyCalendar,
    "CancelMeeting": content.CancelMeeting,
    "ChangeMeeting": content.ChangeMeeting,
    "ChangeMeetingTitle": content.ChangeMeetingTitle,
    "ChangeMeetingType": content.ChangeMeetingType,
    "ContactLookup": content.ContactLookup,
    "CreateReminder": content.CreateReminder,
    "CreateTask": content.CreateTask,
    "Dailyroutines": content.Dailyroutines,
    "DisplayFreeTimes": content.DisplayFreeTimes,
    "DisplayMeetings": content.DisplayMeetings,
    "DriveLookup": content.DriveLookup,
    "GetAnnouncement": content.GetAnnouncement,
    "GetEmail": content.GetEmail,
    "GetKnowledgeForUser": content.GetKnowledgeForUser,
    "GetMeetingNotes": content.GetMeetingNotes,
    "KoraHelp": content.KoraHelp,
    "KoraWelcome": content.KoraWelcome,
    "MeetingLookup": content.MeetingLookup,
    "ModifyInvitees": content.ModifyInvitees,
    "RemoveAllInvitee": content.RemoveAllInvitee,
    "RemoveInvitee": content.RemoveInvitee,
    "RescheduleMeeting": content.RescheduleMeeting,
    "ResetUserSTM": content.ResetUserSTM,
    "RespondToMeeting": content.RespondToMeeting,
    "RespondToTask": content.RespondToTask,
    "ScheduleMeeting": content.ScheduleMeeting,
    "SendEmail": content.SendEmail,
    "SetTestUser": content.SetTestUser,
    "SkillTransfer": content.SkillTransfer,
    "SwitchPersonality": content.SwitchPersonality,
    "TaskLookup": content.TaskLookup,
    "UniversalSearch": content.UniversalSearch,
    "UpdateMeetingDetails": content.UpdateMeetingDetails,
    "UpdateTask": content.UpdateTask
}
/*{
    "AddAnnoucement" : "Add Announcement",
    "AddInvitee" : "Add Invitee",
    "AddKnowledge": "Add Knowledge",
    "AddMeetingNotes": "Add Meeting Notes",
    "AskExpert": "Ask Expert",
    "BlockMyCalendar": "Block my Calendar",
    "CancelMeeting": "Cancel Meeting",
    "ChangeMeeting": "Change Meeting",
    "ChangeMeetingTitle": "Change Meeting Title",
    "ChangeMeetingType": "Change Meeting Type",
    "ContactLookup": "Search Contacts",
    "CreateReminder": "Create Reminder",
    "CreateTask": "Create Task",
    "Dailyroutines": "Daily Routines",
    "DisplayFreeTimes": "Display Time Availability",
    "DisplayMeetings": "Display Meetings",
    "DriveLookup": "Search in Drive",
    "GetAnnouncement": "Check Announcements",
    "GetEmail": "Search Email",
    "GetKnowledgeForUser": "Search Knowledge Articles",
    "GetMeetingNotes": "Get Meeting Notes",
    "KoraHelp": "Help",
    "KoraWelcome": "Welcome",
    "MeetingLookup": "Meeting Search",
    "ModifyInvitees": "Modify Invitees",
    "RemoveAllInvitee": "Remove All Invitees",
    "RemoveInvitee": "Remove Invitee",
    "RescheduleMeeting": "Reschedule Meeting",
    "ResetUserSTM": "Reset User STM",
    "RespondToMeeting": "Respond to Meeting",
    "RespondToTask": "Respond to Task",
    "ScheduleMeeting": "Schedule Meeting",
    "SendEmail": "Send Email",
    "SetTestUser": "Set Test User",
    "SkillTransfer": "Skill Transfer",
    "SwitchPersonality": "Switch Personality",
    "TaskLookup": "Task Search",
    "UniversalSearch": "Search Everywhere",
    "UpdateMeetingDetails": "Update Meeting Details",
    "UpdateTask": "Update Task"
};*/

/* END */



/* Entity specific common parsing */

// Parse the components of a SearchDate entity and return an object with a from and to date, as moments

function parseSearchDate(searchDate) {
    "use strict";

    if (!koreUtil._.isPlainObject(searchDate)) {
        return null;
    }

    var result = {};
    var fromDate;
    var toDate;

    if (searchDate.Date_Period) {
        fromDate = koreUtil.moment(searchDate.Date_Period.fromDate).startOf('day');
        toDate = koreUtil.moment(searchDate.Date_Period.toDate).endOf('day');

    } else if (searchDate.DateEntity) {
        fromDate = koreUtil.moment(searchDate.DateEntity).startOf('day');
        toDate = koreUtil.moment(fromDate).endOf('day');

    } else if (searchDate.duration) {
        // searching by duration can only find items created up to the current time
        toDate = koreUtil.moment();
        fromDate = koreUtil.moment(toDate).subtract(searchDate.duration.amount, searchDate.duration.unit);

    } else if (searchDate.SpecificTime || searchDate.Date_Time) {
        var dateStr = (searchDate.SpecificTime ? searchDate.SpecificTime : searchDate.Date_Time);
        var format = (searchDate.SpecificTime ? 'THH:mm:ssZ' : null);

        if (searchDate.TimePrefix === "from") {
            fromDate = koreUtil.moment(dateStr, format);
            toDate = koreUtil.moment(fromDate).endOf('day');

        } else if (searchDate.TimePrefix === "to") {
            toDate = koreUtil.moment(dateStr, format);
            fromDate = koreUtil.moment(toDate).startOf('day');

        } else {
            // allow some leeway in the time search
            fromDate = koreUtil.moment(dateStr, format);
            toDate = koreUtil.moment(fromDate);
            if (fromDate.hour() < 1) {
                fromDate.startOf('day');
            } else {
                fromDate.subtract(1, 'hour');
            }
            if (toDate.hour() > 22) {
                toDate.endOf('day');
            } else {
                toDate.add(1, 'hour');
            }
        }

    } else if (searchDate.Recent) {
        // searching for recent, so set up longish time before now
        // but user might to want to widen it later
        toDate = koreUtil.moment();
        fromDate = koreUtil.moment(toDate).subtract(env.KARecentTime, 'minute');
        result.doingRecent = true;

    }

    if (fromDate && toDate) {
        result.fromDate = fromDate;
        result.toDate = toDate;
    }

    return result;
}


// extend a recent request with a new date range, returns a new starting date
// also sets context variables to include a description of the previous range 
// and a count of how many times the range has been extended
function extendRecentRequest(prevRequest, fromPath, toPath) {
    "use strict";

    if (!prevRequest) {
        return null;
    }

    // must have a payload with two dates
    var payload;
    if (koreUtil._.isString(prevRequest)) {
        payload = JSON.parse(prevRequest);
    } else if (koreUtil._.isPlainObject(prevRequest)) {
        payload = prevRequest;
    }
    if (!payload || !koreUtil._.has(payload, fromPath) || !koreUtil._.has(payload, toPath)) {
        return null;
    }

    var fromDate = koreUtil.moment(koreUtil._.get(payload, fromPath));
    var toDate = koreUtil.moment(koreUtil._.get(payload, toPath));
    var curRange = toDate.diff(fromDate, 'minute');

    var curDur = koreUtil.moment.duration(curRange, 'minutes');
    var unitName, unitCount;
    if (curDur.asWeeks() < 4) {
        unitName = "week";
        unitCount = curDur.asWeeks();
    } else {
        if (curDur.asMonths() < 18.5) {
            unitName = "month";
            unitCount = curDur.asMonths();
        } else {
            unitName = "year";
            unitCount = curDur.asYears();
        }
    }
    unitCount = Math.round(unitCount);
    var includeNum = (unitCount === 1 ? false : true);
    context.recentPeriodText = pluralize(unitName, unitCount, includeNum);
    context.recentCount = (context.recentCount ? context.recentCount + 1 : 1);

    // the new range will be double the previous range - exponential growth
    fromDate.subtract(2 * curRange, 'minute');

    return fromDate;
}

/* END */



// blakeembrey/pluralize

/* global define */

var pluralize = (function() {
    // Rule storage - pluralize and singularize need to be run sequentially,
    // while other rules can be optimized using an object for instant lookups.
    var pluralRules = [];
    var singularRules = [];
    var uncountables = {};
    var irregularPlurals = {};
    var irregularSingles = {};

    /**
     * Sanitize a pluralization rule to a usable regular expression.
     *
     * @param  {(RegExp|string)} rule
     * @return {RegExp}
     */
    function sanitizeRule(rule) {
        if (typeof rule === 'string') {
            return new RegExp('^' + rule + '$', 'i');
        }

        return rule;
    }

    /**
     * Pass in a word token to produce a function that can replicate the case on
     * another word.
     *
     * @param  {string}   word
     * @param  {string}   token
     * @return {Function}
     */
    function restoreCase(word, token) {
        // Tokens are an exact match.
        if (word === token) return token;

        // Lower cased words. E.g. "hello".
        if (word === word.toLowerCase()) return token.toLowerCase();

        // Upper cased words. E.g. "WHISKY".
        if (word === word.toUpperCase()) return token.toUpperCase();

        // Title cased words. E.g. "Title".
        if (word[0] === word[0].toUpperCase()) {
            return token.charAt(0).toUpperCase() + token.substr(1).toLowerCase();
        }

        // Lower cased words. E.g. "test".
        return token.toLowerCase();
    }

    /**
     * Interpolate a regexp string.
     *
     * @param  {string} str
     * @param  {Array}  args
     * @return {string}
     */
    function interpolate(str, args) {
        return str.replace(/\$(\d{1,2})/g, function(match, index) {
            return args[index] || '';
        });
    }

    /**
     * Replace a word using a rule.
     *
     * @param  {string} word
     * @param  {Array}  rule
     * @return {string}
     */
    function replace(word, rule) {
        return word.replace(rule[0], function(match, index) {
            var result = interpolate(rule[1], arguments);

            if (match === '') {
                return restoreCase(word[index - 1], result);
            }

            return restoreCase(match, result);
        });
    }

    /**
     * Sanitize a word by passing in the word and sanitization rules.
     *
     * @param  {string}   token
     * @param  {string}   word
     * @param  {Array}    rules
     * @return {string}
     */
    function sanitizeWord(token, word, rules) {
        // Empty string or doesn't need fixing.
        if (!token.length || uncountables.hasOwnProperty(token)) {
            return word;
        }

        var len = rules.length;

        // Iterate over the sanitization rules and use the first one to match.
        while (len--) {
            var rule = rules[len];

            if (rule[0].test(word)) return replace(word, rule);
        }

        return word;
    }

    /**
     * Replace a word with the updated word.
     *
     * @param  {Object}   replaceMap
     * @param  {Object}   keepMap
     * @param  {Array}    rules
     * @return {Function}
     */
    function replaceWord(replaceMap, keepMap, rules) {
        return function(word) {
            // Get the correct token and case restoration functions.
            var token = word.toLowerCase();

            // Check against the keep object map.
            if (keepMap.hasOwnProperty(token)) {
                return restoreCase(word, token);
            }

            // Check against the replacement map for a direct word replacement.
            if (replaceMap.hasOwnProperty(token)) {
                return restoreCase(word, replaceMap[token]);
            }

            // Run all the rules against the word.
            return sanitizeWord(token, word, rules);
        };
    }

    /**
     * Check if a word is part of the map.
     */
    function checkWord(replaceMap, keepMap, rules, bool) {
        return function(word) {
            var token = word.toLowerCase();

            if (keepMap.hasOwnProperty(token)) return true;
            if (replaceMap.hasOwnProperty(token)) return false;

            return sanitizeWord(token, token, rules) === token;
        };
    }

    /**
     * Pluralize or singularize a word based on the passed in count.
     *
     * @param  {string}  word      The word to pluralize
     * @param  {number}  count     How many of the word exist
     * @param  {boolean} inclusive Whether to prefix with the number (e.g. 3 ducks)
     * @return {string}
     */
    function pluralize(word, count, inclusive) {
        var pluralized = count === 1 ?
            pluralize.singular(word) : pluralize.plural(word);

        return (inclusive ? count + ' ' : '') + pluralized;
    }

    /**
     * Pluralize a word.
     *
     * @type {Function}
     */
    pluralize.plural = replaceWord(
        irregularSingles, irregularPlurals, pluralRules
    );

    /**
     * Check if a word is plural.
     *
     * @type {Function}
     */
    pluralize.isPlural = checkWord(
        irregularSingles, irregularPlurals, pluralRules
    );

    /**
     * Singularize a word.
     *
     * @type {Function}
     */
    pluralize.singular = replaceWord(
        irregularPlurals, irregularSingles, singularRules
    );

    /**
     * Check if a word is singular.
     *
     * @type {Function}
     */
    pluralize.isSingular = checkWord(
        irregularPlurals, irregularSingles, singularRules
    );

    /**
     * Add a pluralization rule to the collection.
     *
     * @param {(string|RegExp)} rule
     * @param {string}          replacement
     */
    pluralize.addPluralRule = function(rule, replacement) {
        pluralRules.push([sanitizeRule(rule), replacement]);
    };

    /**
     * Add a singularization rule to the collection.
     *
     * @param {(string|RegExp)} rule
     * @param {string}          replacement
     */
    pluralize.addSingularRule = function(rule, replacement) {
        singularRules.push([sanitizeRule(rule), replacement]);
    };

    /**
     * Add an uncountable word rule.
     *
     * @param {(string|RegExp)} word
     */
    pluralize.addUncountableRule = function(word) {
        if (typeof word === 'string') {
            uncountables[word.toLowerCase()] = true;
            return;
        }

        // Set singular and plural references for the word.
        pluralize.addPluralRule(word, '$0');
        pluralize.addSingularRule(word, '$0');
    };

    /**
     * Add an irregular word definition.
     *
     * @param {string} single
     * @param {string} plural
     */
    pluralize.addIrregularRule = function(single, plural) {
        plural = plural.toLowerCase();
        single = single.toLowerCase();

        irregularSingles[single] = plural;
        irregularPlurals[plural] = single;
    };

    /**
     * Irregular rules.
     */
    [
        // Pronouns.
        ['I', 'we'],
        ['me', 'us'],
        ['he', 'they'],
        ['she', 'they'],
        ['them', 'them'],
        ['myself', 'ourselves'],
        ['yourself', 'yourselves'],
        ['itself', 'themselves'],
        ['herself', 'themselves'],
        ['himself', 'themselves'],
        ['themself', 'themselves'],
        ['is', 'are'],
        ['was', 'were'],
        ['has', 'have'],
        ['this', 'these'],
        ['that', 'those'],
        // Words ending in with a consonant and `o`.
        ['echo', 'echoes'],
        ['dingo', 'dingoes'],
        ['volcano', 'volcanoes'],
        ['tornado', 'tornadoes'],
        ['torpedo', 'torpedoes'],
        // Ends with `us`.
        ['genus', 'genera'],
        ['viscus', 'viscera'],
        // Ends with `ma`.
        ['stigma', 'stigmata'],
        ['stoma', 'stomata'],
        ['dogma', 'dogmata'],
        ['lemma', 'lemmata'],
        ['schema', 'schemata'],
        ['anathema', 'anathemata'],
        // Other irregular rules.
        ['ox', 'oxen'],
        ['axe', 'axes'],
        ['die', 'dice'],
        ['yes', 'yeses'],
        ['foot', 'feet'],
        ['eave', 'eaves'],
        ['goose', 'geese'],
        ['tooth', 'teeth'],
        ['quiz', 'quizzes'],
        ['human', 'humans'],
        ['proof', 'proofs'],
        ['carve', 'carves'],
        ['valve', 'valves'],
        ['looey', 'looies'],
        ['thief', 'thieves'],
        ['groove', 'grooves'],
        ['pickaxe', 'pickaxes'],
        ['passerby', 'passersby']
    ].forEach(function(rule) {
        return pluralize.addIrregularRule(rule[0], rule[1]);
    });

    /**
     * Pluralization rules.
     */
    [
        [/s?$/i, 's'],
        [/[^\u0000-\u007F]$/i, '$0'],
        [/([^aeiou]ese)$/i, '$1'],
        [/(ax|test)is$/i, '$1es'],
        [/(alias|[^aou]us|t[lm]as|gas|ris)$/i, '$1es'],
        [/(e[mn]u)s?$/i, '$1s'],
        [/([^l]ias|[aeiou]las|[ejzr]as|[iu]am)$/i, '$1'],
        [/(alumn|syllab|vir|radi|nucle|fung|cact|stimul|termin|bacill|foc|uter|loc|strat)(?:us|i)$/i, '$1i'],
        [/(alumn|alg|vertebr)(?:a|ae)$/i, '$1ae'],
        [/(seraph|cherub)(?:im)?$/i, '$1im'],
        [/(her|at|gr)o$/i, '$1oes'],
        [/(agend|addend|millenni|dat|extrem|bacteri|desiderat|strat|candelabr|errat|ov|symposi|curricul|automat|quor)(?:a|um)$/i, '$1a'],
        [/(apheli|hyperbat|periheli|asyndet|noumen|phenomen|criteri|organ|prolegomen|hedr|automat)(?:a|on)$/i, '$1a'],
        [/sis$/i, 'ses'],
        [/(?:(kni|wi|li)fe|(ar|l|ea|eo|oa|hoo)f)$/i, '$1$2ves'],
        [/([^aeiouy]|qu)y$/i, '$1ies'],
        [/([^ch][ieo][ln])ey$/i, '$1ies'],
        [/(x|ch|ss|sh|zz)$/i, '$1es'],
        [/(matr|cod|mur|sil|vert|ind|append)(?:ix|ex)$/i, '$1ices'],
        [/\b((?:tit)?m|l)(?:ice|ouse)$/i, '$1ice'],
        [/(pe)(?:rson|ople)$/i, '$1ople'],
        [/(child)(?:ren)?$/i, '$1ren'],
        [/eaux$/i, '$0'],
        [/m[ae]n$/i, 'men'],
        ['thou', 'you']
    ].forEach(function(rule) {
        return pluralize.addPluralRule(rule[0], rule[1]);
    });

    /**
     * Singularization rules.
     */
    [
        [/s$/i, ''],
        [/(ss)$/i, '$1'],
        [/(wi|kni|(?:after|half|high|low|mid|non|night|[^\w]|^)li)ves$/i, '$1fe'],
        [/(ar|(?:wo|[ae])l|[eo][ao])ves$/i, '$1f'],
        [/ies$/i, 'y'],
        [/\b([pl]|zomb|(?:neck|cross)?t|coll|faer|food|gen|goon|group|lass|talk|goal|cut)ies$/i, '$1ie'],
        [/\b(mon|smil)ies$/i, '$1ey'],
        [/\b((?:tit)?m|l)ice$/i, '$1ouse'],
        [/(seraph|cherub)im$/i, '$1'],
        [/(x|ch|ss|sh|zz|tto|go|cho|alias|[^aou]us|t[lm]as|gas|(?:her|at|gr)o|[aeiou]ris)(?:es)?$/i, '$1'],
        [/(analy|diagno|parenthe|progno|synop|the|empha|cri|ne)(?:sis|ses)$/i, '$1sis'],
        [/(movie|twelve|abuse|e[mn]u)s$/i, '$1'],
        [/(test)(?:is|es)$/i, '$1is'],
        [/(alumn|syllab|vir|radi|nucle|fung|cact|stimul|termin|bacill|foc|uter|loc|strat)(?:us|i)$/i, '$1us'],
        [/(agend|addend|millenni|dat|extrem|bacteri|desiderat|strat|candelabr|errat|ov|symposi|curricul|quor)a$/i, '$1um'],
        [/(apheli|hyperbat|periheli|asyndet|noumen|phenomen|criteri|organ|prolegomen|hedr|automat)a$/i, '$1on'],
        [/(alumn|alg|vertebr)ae$/i, '$1a'],
        [/(cod|mur|sil|vert|ind)ices$/i, '$1ex'],
        [/(matr|append)ices$/i, '$1ix'],
        [/(pe)(rson|ople)$/i, '$1rson'],
        [/(child)ren$/i, '$1'],
        [/(eau)x?$/i, '$1'],
        [/men$/i, 'man']
    ].forEach(function(rule) {
        return pluralize.addSingularRule(rule[0], rule[1]);
    });

    /**
     * Uncountable rules.
     */
    [
        // Singular words with no plurals.
        'adulthood',
        'advice',
        'agenda',
        'aid',
        'aircraft',
        'alcohol',
        'ammo',
        'analytics',
        'anime',
        'athletics',
        'audio',
        'bison',
        'blood',
        'bream',
        'buffalo',
        'butter',
        'carp',
        'cash',
        'chassis',
        'chess',
        'clothing',
        'cod',
        'commerce',
        'cooperation',
        'corps',
        'debris',
        'diabetes',
        'digestion',
        'elk',
        'energy',
        'equipment',
        'excretion',
        'expertise',
        'firmware',
        'flounder',
        'fun',
        'gallows',
        'garbage',
        'graffiti',
        'hardware',
        'headquarters',
        'health',
        'herpes',
        'highjinks',
        'homework',
        'housework',
        'information',
        'jeans',
        'justice',
        'kudos',
        'labour',
        'literature',
        'machinery',
        'mackerel',
        'mail',
        'media',
        'mews',
        'moose',
        'music',
        'mud',
        'manga',
        'news',
        'only',
        'personnel',
        'pike',
        'plankton',
        'pliers',
        'police',
        'pollution',
        'premises',
        'rain',
        'research',
        'rice',
        'salmon',
        'scissors',
        'series',
        'sewage',
        'shambles',
        'shrimp',
        'software',
        'species',
        'staff',
        'swine',
        'tennis',
        'traffic',
        'transportation',
        'trout',
        'tuna',
        'wealth',
        'welfare',
        'whiting',
        'wildebeest',
        'wildlife',
        'you',
        /pok[eé]mon$/i,
        // Regexes.
        /[^aeiou]ese$/i, // "chinese", "japanese"
        /deer$/i, // "deer", "reindeer"
        /fish$/i, // "fish", "blowfish", "angelfish"
        /measles$/i,
        /o[iu]s$/i, // "carnivorous"
        /pox$/i, // "chickpox", "smallpox"
        /sheep$/i
    ].forEach(pluralize.addUncountableRule);

    return pluralize;
})();

//Some debugger functions. Set context.developerMore = true to run.

function createDebugObjects() {
    if(context.developerMode) {
        if(!context.debug) {
            context.debug = {};
        }
        if(!context.debug[context.currentNodeName]) {
            context.debug[context.currentNodeName] = {};
        }
        if(!context.debug[context.currentNodeName].path) {
            context.debug[context.currentNodeName].path = [];
        }
    }
}

function debugPath(num) {
    if(context.developerMode) {
        createDebugObjects();
        context.debug[context.currentNodeName].path.push(num);
    }
}

function debugVal(varName, val) {
    if(context.developerMode) {
        createDebugObjects();
        context.debug[context.currentNodeName][varName] = val;
    }
}

function clearDebug(){
    if(context.debug && context.debug[context.currentNodeName]) {
        context.debug[context.currentNodeName] = {};
    }
}


//Delete the debug entity. For development purposes.
delete context.entities.DebugEntity