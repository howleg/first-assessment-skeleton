import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server

let prevCmd

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [h] [p]')
  
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
	  
	  username = args.username
      let port = 8080
      let host = 'localhost'
    	  
    	  
      if (args.options.port != undefined && args.options.host != undefined) {
          host = args.options.h
          port = args.options.p
      }
	  
	  
    server = connect({ host: host, port: port }, () => {
      server.write(new Message({username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
    	
      var msg= Message.fromJSON(buffer)
      
      
      if(msg.command === '')
    	  {
    	  this.log('no command')
    	  }
      
      
      if(msg.command.charAt(0) === '@')
    	  {
    	  this.log(cli.chalk['magenta'](msg.toString()))
    	  }
      
      switch(msg.command) {
    case 'connect':
    	this.log(cli.chalk['bgRed'](msg.toString()))
        break;
    case 'disconnect':
    	this.log(cli.chalk['bgRed'](msg.toString()))
        break;
    case 'broadcast':
    	this.log(cli.chalk['bgGreen'](msg.toString()))
    	break;
    case 'echo':
    	this.log(cli.chalk['green'] (msg.toString()))
    	break;
    case 'users':
    	this.log(cli.chalk['blue'] (msg.toString()))
    	break;
      }// end switch
 
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /[^, ]+/g) // no idea the second param. played with 
															// repel on lodash and it works so f%%% it
															
    const contents = rest.join(' ')

    let cmd = command // command is readonly? did this for something.
                      // not needed now
    
    if (cmd === 'disconnect') {
    	prevCmd = command;
      server.end(new Message({ username, command }).toJSON() + '\n')
      
    } else if (cmd === 'echo') {
    	prevCmd = command;	
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } 
    
    else if (cmd === 'users') {
    	prevCmd = command;
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
      } 
    
    else if (cmd === 'broadcast') {
    	prevCmd = command;	
    	server.write(new Message({ username, command, contents }).toJSON() + '\n')	
    }
    
    else if (cmd.charAt(0) === '@') {
    	prevCmd = command;
    	server.write(new Message({ username, command, contents }).toJSON() + '\n')	
    }

    else if(prevCmd != undefined)
    {
    	//2 hours... check commit
    	const newContents = command + ' ' + contents
    	
    	  if (prevCmd === 'echo') {
            server.write(new Message({ username, command:prevCmd, contents:newContents }).toJSON() + '\n')
        }   
        else if (prevCmd === 'users') {
            server.write(new Message({ username, command:prevCmd, contents:newContents }).toJSON() + '\n')
          } 
        else if (prevCmd === 'broadcast') {	 	
        	server.write(new Message({ username, command:prevCmd, contents:newContents }).toJSON() + '\n')	
        }
        else if (prevCmd.charAt(0) === '@') {
        	server.write(new Message({ username, command:prevCmd, contents:newContents }).toJSON() + '\n')	
        }
    	
    } // inner if/elseIf
    
    else {
      cli.log(cli.chalk.bold.red('Command <' + command + '> was not recognized'))  
    }
    

    callback()
  })
