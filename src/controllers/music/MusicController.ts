import { OK, BAD_REQUEST } from 'http-status-codes';
import { Controller, Get, Post } from '@overnightjs/core';
import { Logger } from '@overnightjs/logger';
import { Request, Response } from 'express';
import fileUpload from 'express-fileupload';
import path from 'path'

@Controller('api/music')
class MusicController {

    public static readonly SUCCESS_MSG = 'hello ';

    @Get(':id')
    private getSong(req: Request, res: Response){
        try {
            const { id } = req.params;
            //todo check the id on the DB to know which song is
            res.sendFile(path.join(__dirname, '../../../public', 'song.mp3'));
        } catch (err) {
            // On wrong id send error
        }
    }

    @Post('')
    private addSong(req: Request, res: Response){
        if(!req.files && req.files!.song){
            res.sendStatus(BAD_REQUEST);
        }
        let song : fileUpload.UploadedFile = req.files!.song as fileUpload.UploadedFile;
        
        Logger.Info(song.name);
        song.mv(path.join(__dirname, '../../../public', Date.now() + "_" + song.name))
        res.sendStatus(OK);
    }
}

export default MusicController;