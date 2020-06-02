import { OK, BAD_REQUEST, NOT_IMPLEMENTED } from 'http-status-codes';
import { Controller, Get, Post } from '@overnightjs/core';
import { Request, Response } from 'express';
import fileUpload from 'express-fileupload';
import path from 'path'
import Song, {ISong, SongHelper} from '../../models/Song.model';
import AWS from 'aws-sdk';
import { PutObjectRequest, GetObjectRequest } from 'aws-sdk/clients/s3';
import {fs} from 'memfs';
require('dotenv').config()
AWS.config.update({
    region: 'us-west-1',
    accessKeyId: process.env.AWSAccessKeyId,
    secretAccessKey: process.env.AWSSecretKey
});
const S3 = new AWS.S3();


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
            const exists = fs.existsSync(path.join(__dirname, '../../../public/songs', song!.songFile));
            if(exists){
                res.sendFile(path.join(__dirname, '../../../public/songs', song!.songFile));            
                return;
            }
            (await downloadFile(process.env.AWS_BUCKET || "", "public/songs/"+song!.songFile)).pipe(res);
        } catch (err) {
            res.sendStatus(BAD_REQUEST);
        }
    }

    @Get('image/:id')
    private async getImage(req: Request, res: Response){
        try {
            const song = await SongHelper.findById(req.params.id);
            const exists = fs.existsSync(path.join(__dirname, '../../../public/images', song!.imgFile));
            if(exists){
                res.sendFile(path.join(__dirname, '../../../public/images', song!.imgFile));    
                return;
            }
            (await downloadFile(process.env.AWS_BUCKET || "", "public/images/"+song!.imgFile)).pipe(res);
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
        imgFile.mv(path.join(__dirname, '../../../public/images', song.imgFile));
        songFile.mv(path.join(__dirname, '../../../public/songs', song.songFile));
        uploadFile(process.env.AWS_BUCKET  || "", "public/songs/"+song.songFile, path.join(__dirname, '../../../public/songs', song.songFile));
        uploadFile(process.env.AWS_BUCKET  || "", "public/images/"+song.imgFile, path.join(__dirname, '../../../public/images', song.imgFile));
        song.save();
        
        res.send({
            "id": songId
        });
    }
}

async function downloadFile(Bucket: string, Key: string) {
    const request : GetObjectRequest = {
        Bucket: Bucket,
        Key: Key
    };
    const result = await S3
    .getObject(request)
    .promise();

    fs.writeFileSync(`../../../${Key}`, result.Body as any);
    const file = await fs.createReadStream(`/${Key}`);
    return file;
}

function uploadFile(bucket: string, key: string, path: string){
    fs.readFile(path, (err, data) => {
        if(err) {
            throw err;
        }
        const songParams : PutObjectRequest = {
            Bucket: bucket,
            Body : data,
            Key : key
        };
        S3.upload(songParams, function (err, data) {
            if (err) {
                console.log("Error", err);
            }
            if (data) {
                console.log("Uploaded in:", data.Location);
            }
        });
    })
}

export default MusicController;