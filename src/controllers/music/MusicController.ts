import { OK, BAD_REQUEST } from 'http-status-codes';
import { Controller, Get, Post } from '@overnightjs/core';
import { Logger } from '@overnightjs/logger';
import { Request, Response } from 'express';
import fileUpload from 'express-fileupload';
import path from 'path'
import Song, {ISong, SongHelper} from '../../models/Song.model';

@Controller('api/music')
class MusicController {

    public static readonly SUCCESS_MSG = 'hello ';

    @Get('all')
    private async getAllSongs(req: Request, res: Response){
        Song.find({}).select('-_id -imgFile -songFile -__v').then(songs=>{
            res.send(songs);
        })
    }

    @Get('data/:id')
    private async getData(req: Request, res: Response){
        try {
            const song = await SongHelper.findById(req.params.id);
            if(!song){
                res.send({})
                return;
            }
            res.send({
                id: song!.id,
                name: song!.name,
                artist: song!.artist,
            })            
        } catch (err) {
            console.log(err);
            res.sendStatus(BAD_REQUEST);
        }
    }

    @Get('song/:id')
    private async getSong(req: Request, res: Response){
        try {
            const song = await SongHelper.findById(req.params.id);
            res.sendFile(path.join(__dirname, '../../../public/songs', song!.songFile));            
        } catch (err) {
            res.sendStatus(BAD_REQUEST);
        }
    }

    @Get('image/:id')
    private async getImage(req: Request, res: Response){
        try {
            const song = await SongHelper.findById(req.params.id);
            res.sendFile(path.join(__dirname, '../../../public/images', song!.imgFile));            
        } catch (err) {
            res.sendStatus(BAD_REQUEST);
        }
    }

    @Post('')
    private addSong(req: Request, res: Response){
        if(!req.files && req.files!.song && req.files!.img){
            res.sendStatus(BAD_REQUEST);
        }
        const songFile = req.files!.song as fileUpload.UploadedFile;
        const imgFile = req.files!.img as fileUpload.UploadedFile;
        let songId = Date.now()+"";
        const song: ISong = new Song({
            id: songId,
            name: req.body.name,
            artist: req.body.artist,
            imgFile: songId+"_"+imgFile.name,
            songFile: songId+"_"+songFile.name
        })
        song.save();
        imgFile.mv(path.join(__dirname, '../../../public/images', song.imgFile));
        songFile.mv(path.join(__dirname, '../../../public/songs', song.songFile));
        res.send({
            "id": songId
        });
    }
}

export default MusicController;